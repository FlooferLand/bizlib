package com.flooferland.bizlib.bits

import com.flooferland.bizlib.bits.generated.BitsmapBaseVisitor
import com.flooferland.bizlib.bits.generated.BitsmapLexer
import com.flooferland.bizlib.bits.generated.BitsmapParser
import com.flooferland.bizlib.position
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree
import java.io.InputStream

// TODO: Add thorough error throwing to tell the user why something doesn't work

// TODO: Burn this class in the darkest pits

/** NOTE: Constructor will throw an exception if failed parsing */
class BitsMap {
    var bitmapFile: BotBitmapFile? = null
    var fixtureMap = mutableMapOf<MappingName, FixtureName>()

    var errorCallback: (message: LogMessage) -> Unit = { message -> }
    var warnHandler: (message: LogMessage) -> Unit = { message -> System.err.println("[Bizlib] WARN: $message") }

    enum class Severity { Error, Warning }

    data class LogMessage(val text: String, var position: Pos? = null, var context: String? = null, val severity: Severity = Severity.Error) {
        data class Point(val line: Int, val column: Int)
        data class Pos(val start: Point, val end: Point) {
            override fun toString() = "line ${start.line}, column ${start.column}"
            fun contextAround(raw: String): String {
                val lines = raw.lines()
                val first = (start.line - 2).coerceAtLeast(1)
                val last = (end.line + 1).coerceAtMost(lines.size)
                val width = last.toString().length
                return buildString {
                    for (i in first..last) {
                        appendLine("${i.toString().padStart(width)} | ${lines[i - 1]}")
                        if (i in start.line..end.line) {
                            val from = if (i == start.line) start.column else 0
                            val to = if (i == end.line) end.column else lines[i - 1].length
                            appendLine(" ".repeat(width) + " | " + " ".repeat(from) + "^".repeat((to - from).coerceAtLeast(1)))
                        }
                    }
                }.trimEnd()
            }
        }
        override fun toString(): String {
            var message = text
            if (position != null) message += " ($position)"
            if (context != null) message += "\n$context"
            return message
        }
    }

    data class Analysis(val tree: ParseTree, val messages: List<LogMessage>)

    inner class Visitor(val raw: String, private val messages: MutableList<LogMessage>) : BitsmapBaseVisitor<Unit>() {
        private val bitmaps = mutableMapOf<MappingName, FixtureMap>()
        private val oldBitmaps = mutableMapOf<MappingName, FixtureMap>()
        private val bitMovements = mutableMapOf<MappingName, MutableMap<UShort, BitMappingData>>()
        private var version: UShort = 0u

        override fun defaultResult() = Unit

        private fun addError(ctx: ParserRuleContext, text: String) = addLog(ctx, text, Severity.Error)
        private fun addWarn(ctx: ParserRuleContext, text: String) = addLog(ctx, text, Severity.Warning)
        private fun addLog(ctx: ParserRuleContext, text: String, severity: Severity) {
            val message = LogMessage(text, severity = severity)
            ctx.position?.let { pos ->
                val at = LogMessage.Pos(LogMessage.Point(pos.start.line, pos.start.column), LogMessage.Point(pos.end.line, pos.end.column))
                message.position = at
                message.context = at.contextAround(raw)
            }
            messages += message
        }

        private fun fail(ctx: ParserRuleContext, message: String) = addError(ctx, message)

        override fun visitPrepFields(ctx: BitsmapParser.PrepFieldsContext) {
            ctx.versionPrep()?.let { versionPrep ->
                version = versionPrep.INTEGER().text.toUShortOrNull() ?: version
            }
            super.visitPrepFields(ctx)
        }

        override fun visitSetStmt(ctx: BitsmapParser.SetStmtContext) {
            val mapKey = ctx.map().text

            BitUtils.readBitmap(mapKey)?.let { bitmaps[mapKey] = it }
            BitUtils.readBitmap("${mapKey}_old")?.let { oldBitmaps[mapKey] = it }
            if (mapKey !in bitmaps && mapKey !in oldBitmaps)
                addWarn(ctx.map(), "Unknown map '$mapKey'.")

            ctx.fixture()?.let { fixture ->
                val fixtureName = fixture.ID().text
                val current = bitmaps[mapKey]?.keys.orEmpty()
                val legacy = oldBitmaps[mapKey]?.keys.orEmpty()
                when {
                    fixtureName in current || (current + legacy).isEmpty() -> Unit
                    fixtureName in legacy -> addWarn(fixture, "Fixture '$fixtureName' only exists in the old bitmap. Please migrate to the new fixture names.")
                    else -> addWarn(fixture, "Unknown fixture '$fixtureName'." + if (current.size in 1..2) " Expected one of: ${current.joinToString()}." else "")
                }
                fixtureMap[mapKey] = fixtureName
            }
            super.visitSetStmt(ctx)
        }

        override fun visitBitStmt(ctx: BitsmapParser.BitStmtContext) {
            val rotates = mutableListOf<RotateCommand>()
            val moves = mutableListOf<MoveCommand>()
            val anim = mutableListOf<AnimCommand>()
            var flow: FlowCommand? = null
            var oldFlow: OldFlowCommand? = null
            var type: MoveType = MoveType.Default
            var hold: BooleanType = BooleanType.defaultNo()
            var wiggleMul = 1.0
            for (field in ctx.bitFields()) {
                when {
                    field.flowField() != null -> {
                        val flowCtx = field.flowField()!!
                        val new = flowCtx.param()
                        val old = flowCtx.oldFlowFieldParams()
                        if (new.isNotEmpty()) {
                            val params = FlowParams().apply { fill(flowCtx, new, ::fail) }
                            flow = params.toCommand()
                        } else if (old != null) {
                            oldFlow = OldFlowCommand(
                                speed = old.num()?.text?.toDoubleOrNull() ?: 0.0,
                                easing = when (old.EASING()?.text) {
                                    "ease-in" -> Easing.EaseIn
                                    "linear" -> Easing.Linear
                                    else -> Easing.Default
                                }
                            )
                            addWarn(old, "You are using the old flow system, please migrate to RR Engine flows!")
                        } else {
                            addWarn(flowCtx, "You should probably specify some flows")
                        }
                    }
                    field.animField() != null -> {
                        val animValue = field.animField()!!.STRING().text.removeSurrounding("\"")
                        anim += AnimCommand(id = animValue)
                    }
                    field.rotateField() != null -> {
                        val rotate = field.rotateField()!!
                        val axes = TransformParams("rotate").apply { fill(rotate, rotate.param(), ::fail) }
                        rotates += RotateCommand(
                            bone = rotate.bone().STRING().text.removeSurrounding("\""),
                            target = Coords3(x = axes.x?.value, y = axes.y?.value, z = axes.z?.value)
                        )
                    }
                    field.moveField() != null -> {
                        val move = field.moveField()!!
                        val axes = TransformParams("move").apply { fill(move, move.param(), ::fail) }
                        moves += MoveCommand(
                            bone = move.bone().STRING().text.removeSurrounding("\""),
                            target = Coords3(x = axes.x?.value, y = axes.y?.value, z = axes.z?.value)
                        )
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
                val mapKey = mappedMovement.map().text
                if (mapKey != "any" && BitUtils.readBitmap(mapKey) == null && BitUtils.readBitmap("${mapKey}_old") == null) {
                    addError(mappedMovement.map(), "Unknown map '$mapKey'.")
                }
                val mapping = BitMappingData(
                    flow = flow,
                    oldFlow = oldFlow,
                    rotates = rotates,
                    moves = moves,
                    anim = anim,
                    type = type,
                    hold = hold,
                    wiggleMul = wiggleMul,
                    name = fixtureMap[mapKey]
                )

                val bitCtx = mappedMovement.bit()
                val moveName = bitCtx.ID()?.text
                val moveId = BitUtils.parseBitId(bitCtx.text)
                if (moveName == null && moveId == null) {
                    addWarn(bitCtx, "No movement id found for mapping $mapKey (please enter a valid bit ID)")
                    continue
                }
                if (moveId != null && moveId !in 1u.toUShort()..BitUtils.DRAWER_MAX) {
                    addError(bitCtx, "Bit ID '${bitCtx.text}' is out of range (1 to ${BitUtils.DRAWER_MAX}).")
                    continue
                }
                if (moveName != null) addWarn(bitCtx, "Name IDs are no longer supported. Use a bit number")

                fun add(mapKey: MappingName, fixtureName: FixtureName? = null) {
                    val bit = moveId ?: run {
                        // Named bits
                        val fixtureName = fixtureName ?: fixtureMap[mapKey] ?: run {
                            addError(bitCtx, "No fixture was specified for the map '${mapKey}'. No idea what bit to map '${moveName}' to.")
                            return
                        }
                        val oldBitmapWarn = "A movement from the old bitmap was found being used for '${mapKey}.${fixtureName}'. Please migrate your addon to use numerical bit IDs!"
                        val fixtures = bitmaps[mapKey]
                            ?: oldBitmaps[mapKey]?.also { addWarn(bitCtx, oldBitmapWarn) }
                            ?: run {
                                addError(bitCtx, "No bitmap found for '${mapKey}'. Consider using explicit bitmaps and bit IDs instead of bit names")
                                return
                            }

                        fixtures[fixtureName]?.get(moveName)
                            ?: oldBitmaps[mapKey]?.get(fixtureName)?.get(moveName)?.also { addWarn(bitCtx, oldBitmapWarn) }
                            ?: run {
                                addError(bitCtx, "No move with the name '${moveName}' was found for fixture '${fixtureName}'")
                                return
                            }
                    }

                    val movementsTarget = bitMovements.getOrPut(mapKey) { mutableMapOf() }
                    movementsTarget[bit] = mapping
                }

                if (mapKey == "any") {  // Adding all maps
                    fixtureMap.forEach { (mapKey, fixtureName) -> add(mapKey, fixtureName) }
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

    /** Parses and checks [text], used by the lsp */
    fun analyze(text: String): Analysis {
        val messages = mutableListOf<LogMessage>()
        val errors = BitsMapErrorListener(text, messages)
        val lexer = BitsmapLexer(CharStreams.fromString(text)).apply { removeErrorListeners(); addErrorListener(errors) }
        val parser = BitsmapParser(CommonTokenStream(lexer)).apply { removeErrorListeners(); addErrorListener(errors) }
        val tree = parser.file()
        Visitor(text, messages).visit(tree)
        return Analysis(tree, messages)
    }

    /** NOTE: Will throw if failed parsing */
    fun load(stream: InputStream): BotBitmapFile {
        val text = stream.bufferedReader().use { it.readText() }
        val messages = analyze(text).messages
        messages.forEach { if (it.severity == Severity.Error) errorCallback(it) else warnHandler(it) }
        val errors = messages.filter { it.severity == Severity.Error }
        if (errors.isNotEmpty()) error(errors.joinToString("\n"))
        return bitmapFile ?: error("Internal error: ${this::bitmapFile.name} was not filled by the end of the visitor. File may be empty?")
    }
}
