package com.flooferland.bizlib.bits

import com.flooferland.bizlib.bits.generated.*
import org.antlr.v4.kotlinruntime.*
import java.io.InputStream
import org.antlr.v4.kotlinruntime.ast.Position
import kotlin.let

// TODO: Add thorough error throwing to tell the user why something doesn't work

/** NOTE: Constructor will throw an exception if failed parsing */
class BitsMap {
    private var bitmapFile: BotBitmapFile? = null
    private var fixtureMap = mutableMapOf<MappingName, FixtureName>()

    private inner class Visitor(val raw: String) : BitsmapBaseVisitor<Unit>() {
        private val bitmaps = mutableMapOf<MappingName, FixtureMap>()
        private val bitMovements = mutableMapOf<MappingName, MutableMap<UShort, BitMappingData>>()
        private var version: UShort = 0u
        var errorPosition: Position? = null
        var warningPosition: Position? = null

        override fun defaultResult() = Unit

        @Throws(IllegalStateException::class)
        private fun ParserRuleContext.err(message: String): Nothing {
            var message = message
            this.position?.let { pos ->
                message += " (line ${pos.start.line}, column ${pos.start.column})"
                message += "\n"
                message += pos.text(raw).prependIndent(">    ")
                errorPosition = pos
            }
            error(message)
        }

        private fun ParserRuleContext.warn(message: String) {
            var message = message
            this.position?.let { pos ->
                message += " (line ${pos.start.line}, column ${pos.start.column})"
                message += "\n"
                message += pos.text(raw).prependIndent(">    ")
                warningPosition = pos
            }
            println("[Bizlib] WARN: $message")
        }

        override fun visitPrepFields(ctx: BitsmapParser.PrepFieldsContext) {
            ctx.versionPrep()?.let { versionPrep ->
                version = versionPrep.INTEGER().text.toUShortOrNull() ?: version
            }
            super.visitPrepFields(ctx)
        }

        override fun visitSetStmt(ctx: BitsmapParser.SetStmtContext) {
            val mapKey = ctx.MAP().text

            val map = BitUtils.readBitmap(mapKey).also { if (it == null) ctx.warn("No bitmap registered for map '$mapKey'") }
            map?.let { bitmaps[mapKey] = it }

            ctx.fixture()?.let { fixtureMap[mapKey] = it.ID().text }
            super.visitSetStmt(ctx)
        }

        override fun visitBitStmt(ctx: BitsmapParser.BitStmtContext) {
            val rotates = mutableListOf<RotateCommand>()
            val moves = mutableListOf<MoveCommand>()
            var flow = FlowCommand()
            val anim = mutableListOf<AnimCommand>()
            var type: MoveType = MoveType.Default
            var hold: BooleanType = BooleanType.defaultNo()
            var wiggleMul = 1.0
            for (field in ctx.bitFields()) {
                when {
                    field.flowField() != null -> {
                        flow = FlowCommand(
                            speed = field.flowField()!!.num().text.toDoubleOrNull() ?: 0.0,
                            easing = when (field.flowField()!!.EASING()?.text) {
                                "ease-in" -> Easing.EaseIn
                                "linear" -> Easing.Linear
                                else -> Easing.Default
                            }
                        )
                    }
                    field.animField() != null -> {
                        val animValue = field.animField()!!.STRING().text.removeSurrounding("\"")
                        anim += AnimCommand(id = animValue)
                    }
                    field.rotateField() != null -> {
                        val bone = field.rotateField()!!.bone().STRING().text.removeSurrounding("\"")
                        val vec3i = Coords3.fromAntlr(field.rotateField()?.vec3i()!!)
                        rotates += RotateCommand(bone = bone, target = vec3i)
                    }
                    field.moveField() != null -> {
                        val bone = field.moveField()!!.bone().STRING().text.removeSurrounding("\"")
                        val vec3i = Coords3.fromAntlr(field.moveField()?.vec3i()!!)
                        moves += MoveCommand(bone = bone, target = vec3i)
                    }
                    field.typeField() != null -> {
                        type = MoveType.from(field.typeField()!!.MOVE_TYPE().text)
                    }
                    field.holdField() != null -> {
                        hold = BooleanType.from(field.holdField()!!.BOOLEAN().text)
                    }
                    field.wiggleMulField() != null -> {
                        wiggleMul = field.wiggleMulField()!!.num().text.toDoubleOrNull() ?: 0.0
                    }
                }
            }

            // Adding the movements
            for (mappedMovement in ctx.mappedMovement()) {
                val mapKey = mappedMovement.MAP().text
                val mapping = BitMappingData(
                    flow = flow,
                    rotates = rotates,
                    moves = moves,
                    anim = anim,
                    type = type,
                    hold = hold,
                    wiggleMul = wiggleMul,
                    name = fixtureMap[mapKey]
                )

                val moveName = mappedMovement.bit().ID()?.text
                val moveId: UShort? = mappedMovement.bit().INTEGER()?.text?.toUShortOrNull()
                    ?: mappedMovement.bit().DRAWER_BIT()?.text?.let { bitStr ->
                        val num = bitStr.filter { it.isDigit() }.toUShortOrNull() ?: ctx.err("Bit ID '${bitStr}' is not a number, nor a name.")
                        when {
                            bitStr.endsWith("td") -> num
                            bitStr.endsWith("bd") -> (BitUtils.NEXT_DRAWER + num).toUShort()
                            else -> ctx.err("Bit ID '${bitStr}' is not a number, nor a name.")
                        }
                    }
                if (moveName == null && moveId == null) {
                    ctx.err("No movement name/id found for mapping $mapKey (please enter a valid name or the bit ID)")
                }

                fun add(mapKey: MappingName, fixtureName: FixtureName? = null, forceNamed: Boolean = false) {
                    val bit = if (moveId != null && !forceNamed) moveId else {
                        // Named bits
                        val fixtureName = fixtureName ?: fixtureMap[mapKey] ?: ctx.err("No fixture was specified for the map '${mapKey}'. No idea what bit to map '${moveName}' to.")
                        val bitmap = bitmaps[mapKey] ?: ctx.err("No bitmap found for '${mapKey}'. Consider using explicit bitmaps and bit IDs instead of bit names")
                        bitmap[fixtureName]?.get(moveName) ?: ctx.err("No move with the name '${moveName}' was found")
                    }

                    val movementsTarget = bitMovements.getOrPut(mapKey) { mutableMapOf() }
                    movementsTarget[bit] = mapping
                }

                if (mapKey == "any") {  // Adding all maps
                    fixtureMap.forEach { (mapKey, fixtureName) -> add(mapKey, fixtureName, forceNamed = true) }
                } else {
                    add(mapKey)
                }
            }

            bitmapFile = BotBitmapFile(
                settings = mapOf(),
                fixture = fixtureMap,
                bits = bitMovements
            )
            super.visitBitStmt(ctx)
        }
    }

    /** NOTE: Will throw if failed parsing */
    fun load(stream: InputStream): BotBitmapFile {
        val text = stream.bufferedReader().use { it.readText() }
        val lexer = BitsmapLexer(CharStreams.fromString(text))
        val tokens = CommonTokenStream(lexer)
        val parser = BitsmapParser(tokens)
        val tree = parser.file()

        val visitor = Visitor(text)
        visitor.visit(tree)
        return bitmapFile ?: error("Internal error: ${this::bitmapFile.name} was not filled by the end of the visitor. File may be empty?")
    }
}
