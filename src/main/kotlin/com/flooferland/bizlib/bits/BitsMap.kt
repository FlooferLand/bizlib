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
        private val bitMovements = mutableMapOf<MappingName, MutableMap<Short, BitMappingData>>()

        override fun defaultResult() = Unit

        override fun visitSetStmt(ctx: BitsmapParser.SetStmtContext) {
            val map = ctx.MAP().text
            val mapping = BitUtils.readBitmap(map) ?: error("No bitmap registered for map '$map'")
            val fixtureKey = ctx.fixture().ID().text
            val movements = mapping[fixtureKey] ?: error("Fixture '$fixtureKey' wasn't found")
            movementsCurrentFixture[map] = movements
            currentFixture[map] = fixtureKey
            super.visitSetStmt(ctx)
        }

        override fun visitBitStmt(ctx: BitsmapParser.BitStmtContext) {
            var rotate: RotateCommand? = null
            var move: MoveCommand? = null
            var anim: AnimCommand? = null
            var flow = 0.0
            for (field in ctx.bitFields()) {
                when {
                    field.flowField() != null -> {
                        flow = field.flowField()!!.DECIMAL().text.toDoubleOrNull() ?: 0.0
                    }
                    field.animField() != null -> {
                        val animValue = field.animField()!!.STRING().text.removeSurrounding("\"")
                        anim = AnimCommand(id = animValue)
                    }
                    field.rotateField() != null -> {
                        val bone = field.rotateField()!!.bone().STRING().text.removeSurrounding("\"")
                        val vec3i = Coords3.fromAntlr(field.rotateField()?.vec3i()!!)
                        rotate = RotateCommand(bone = bone, target = vec3i)
                    }
                    field.moveField() != null -> {
                        val bone = field.moveField()!!.bone().STRING().text.removeSurrounding("\"")
                        val vec3i = Coords3.fromAntlr(field.moveField()?.vec3i()!!)
                        move = MoveCommand(bone = bone, target = vec3i)
                    }
                }
            }

            // Adding the movements
            for (moves in ctx.mappedMovement()) {
                val mapKey = moves.MAP().text
                val moveKey = moves.movement().ID().text
                val mapping = BitMappingData(
                    flow = flow,
                    rotate = rotate,
                    move = move,
                    anim = anim,
                    name = currentFixture[mapKey]
                )

                if (mapKey != "any") {
                    val bit = (movementsCurrentFixture[mapKey] ?: error("No fixture found for mapping $mapKey"))[moveKey]
                        ?: error("The movement '$moveKey' doesn't exist in the map '$mapKey'")
                    val movementsTarget = bitMovements.getOrPut(mapKey, { mutableMapOf() })
                    movementsTarget[bit] = mapping
                } else {
                    fun add(map: String) {
                        val bit = (movementsCurrentFixture[map] ?: error("No fixture found for mapping $map"))[moveKey]
                            ?: error("The movement '$moveKey' doesn't exist in the map '$map'")
                        val movementsTarget = bitMovements.getOrPut(map, { mutableMapOf() })
                        movementsTarget[bit] = mapping
                    }
                    add("faz")
                    add("rae")
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
