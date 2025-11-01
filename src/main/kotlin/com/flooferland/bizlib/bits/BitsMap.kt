package com.flooferland.bizlib.bits

import com.flooferland.bizlib.bits.generated.*
import org.antlr.v4.kotlinruntime.*
import java.io.InputStream

// TODO: Add thorough error throwing to tell the user why something doesn't work

/** NOTE: Constructor will throw an exception if failed parsing */
class BitsMap(stream: InputStream) {
    var currentFixture = mutableMapOf<MappingName, FixtureName>()
    val mappingsCurrentFixture = mutableMapOf<MappingName, Movements>()
    val bitStatements = mutableMapOf<MappedBit, BitMapping>()

    private inner class Visitor : BitsmapBaseVisitor<Unit>() {
        override fun defaultResult() = Unit

        override fun visitSetStmt(ctx: BitsmapParser.SetStmtContext) {
            val map = ctx.MAP().text
            val mapping = BitUtils.readBitmap(map) ?: error("No bitmap registered for map '$map'")
            val fixtureKey = ctx.fixture().ID().text
            val movements = mapping[fixtureKey] ?: error("Fixture '$fixtureKey' wasn't found")
            mappingsCurrentFixture[map] = movements
            currentFixture[map] = fixtureKey
            super.visitSetStmt(ctx)
        }

        override fun visitBitStmt(ctx: BitsmapParser.BitStmtContext) {
            println(ctx.mappedMovement().joinToString(", ") { "${it.MAP().text} ${it.movement().ID().text}" })
            var rotate: RotateCommand? = null
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
                        val vec3i = run {
                            val vec = field.rotateField()!!.vec3i()
                            val angle = Angle3()
                            vec.iaxisX()?.let { angle.x = it.INTEGER().text.toIntOrNull() ?: 0 }
                            vec.iaxisY()?.let { angle.y = it.INTEGER().text.toIntOrNull() ?: 0 }
                            vec.iaxisZ()?.let { angle.z = it.INTEGER().text.toIntOrNull() ?: 0 }
                            angle
                        }
                        rotate = RotateCommand(bone = bone, target = vec3i)
                    }
                }
            }

            // Adding the movements
            for (moves in ctx.mappedMovement()) {
                val mapKey = moves.MAP().text
                val moveKey = moves.movement().ID().text
                val movements = run {
                    if (mapKey != "any") {
                        mappingsCurrentFixture[mapKey] ?: error("No fixture found for mapping $mapKey")
                    } else {
                        val movements = Movements()
                        mappingsCurrentFixture.values.forEach { movements.putAll(it) }
                        movements
                    }
                }

                val movement = movements[moveKey] ?: error("The movement '$moveKey' doesn't exist in the map '$mapKey'")
                val bit = MappedBit(
                    mapping = moves.MAP().text,
                    bit = movement
                )
                val mapping = BitMapping(
                    flow = flow,
                    rotate = rotate,
                    anim = anim
                )
                bitStatements[bit] = mapping
            }

            super.visitBitStmt(ctx)
        }
    }

    /** NOTE: Will throw if failed parsing */
    init {
        val lexer = BitsmapLexer(CharStreams.fromStream(stream))
        val tokens = CommonTokenStream(lexer)
        val parser = BitsmapParser(tokens)
        val tree = parser.file()

        val visitor = Visitor()
        visitor.visit(tree)
    }
}
