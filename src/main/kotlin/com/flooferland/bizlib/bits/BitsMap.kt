package com.flooferland.bizlib.bits

import com.flooferland.bizlib.bits.generated.*
import org.antlr.v4.kotlinruntime.*
import java.io.InputStream

// TODO: Add thorough error throwing to tell the user why something doesn't work

/** NOTE: Constructor will throw an exception if failed parsing */
class BitsMap {
    private var bitmapFile: BotBitmapFile? = null

    private inner class Visitor : BitsmapBaseVisitor<Unit>() {
        private var currentFixture = mutableMapOf<MappingName, FixtureName>()
        private val movementsCurrentFixture = mutableMapOf<MappingName, Movements>()
        private val bitMovements = mutableMapOf<MappingName, MutableMap<UShort, BitMappingData>>()
        private var version: UShort = 0u

        override fun defaultResult() = Unit

        @Throws(IllegalStateException::class)
        private fun ParserRuleContext.err(message: String): Nothing {
            var message = message
            this.position?.let { message += " (line ${it.start.line}, column ${it.start.column})" }
            error(message)
        }

        private fun ParserRuleContext.warn(message: String) {
            var message = message
            this.position?.let { message += " (line ${it.start.line}, column ${it.start.column})" }
            println("[Bizlib] WARN: $message")
        }

        override fun visitPrepFields(ctx: BitsmapParser.PrepFieldsContext) {
            ctx.versionPrep()?.let { versionPrep ->
                version = versionPrep.INTEGER().text.toUShortOrNull() ?: version
            }
            super.visitPrepFields(ctx)
        }

        override fun visitSetStmt(ctx: BitsmapParser.SetStmtContext) {
            val map = ctx.MAP().text
            val mapping = BitUtils.readBitmap(map) ?: ctx.err("No bitmap registered for map '$map'")
            val fixtureKey = ctx.fixture().ID().text
            val movements = mapping[fixtureKey] ?: ctx.err("Fixture '$fixtureKey' wasn't found")
            movementsCurrentFixture[map] = movements
            currentFixture[map] = fixtureKey
            super.visitSetStmt(ctx)
        }

        override fun visitBitStmt(ctx: BitsmapParser.BitStmtContext) {
            val rotates = mutableListOf<RotateCommand>()
            val moves = mutableListOf<MoveCommand>()
            var flow = FlowCommand()
            var anim = mutableListOf<AnimCommand>()
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
                    name = currentFixture[mapKey]
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

                fun add(mapKey: String) {
                    val map = movementsCurrentFixture[mapKey] ?: ctx.err("No fixture found for mapping $mapKey")
                    val bit: UShort? = map[moveName] ?: moveId
                    if (bit == null) ctx.err("The movement '$moveName' doesn't exist in the map '$mapKey'")
                    val movementsTarget = bitMovements.getOrPut(mapKey, { mutableMapOf() })
                    movementsTarget[bit] = mapping
                }
                if (mapKey != "any") {
                    add(mapKey)
                } else {
                    movementsCurrentFixture.keys.forEach { add(it) }
                }
            }

            bitmapFile = BotBitmapFile(
                settings = mapOf(),
                fixture = currentFixture,
                bits = bitMovements
            )
            super.visitBitStmt(ctx)
        }
    }

    /** NOTE: Will throw if failed parsing */
    fun load(stream: InputStream): BotBitmapFile {
        val lexer = BitsmapLexer(CharStreams.fromStream(stream))
        val tokens = CommonTokenStream(lexer)
        val parser = BitsmapParser(tokens)
        val tree = parser.file()

        val visitor = Visitor()
        visitor.visit(tree)
        return bitmapFile ?: error("Internal error: ${this::bitmapFile.name} as not filled by visitor")
    }
}
