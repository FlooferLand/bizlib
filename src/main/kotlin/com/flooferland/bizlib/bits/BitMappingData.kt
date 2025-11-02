package com.flooferland.bizlib.bits

import com.flooferland.bizlib.bits.generated.BitsmapParser
import kotlinx.serialization.Serializable

@Serializable
data class BitMappingData(
    val flow: Double,
    val name: String? = null,

    val rotates: List<RotateCommand> = listOf(),
    val moves: List<MoveCommand> = listOf(),
    val anim: AnimCommand? = null
)

@Serializable
data class AnimCommand(
    val id: String? = null,
    val on: String? = null,
    val off: String? = null
)

@Serializable
data class RotateCommand(
    val bone: String,
    val target: Coords3
)

@Serializable
data class MoveCommand(
    val bone: String,
    val target: Coords3
)

@Serializable
data class Coords3(var x: Int = 0, var y: Int = 0, var z: Int = 0) {
    companion object {
        fun fromAntlr(vec: BitsmapParser.Vec3iContext): Coords3 {
            val angle = Coords3()
            vec.iaxisX()?.let { angle.x = it.INTEGER().text.toIntOrNull() ?: 0 }
            vec.iaxisY()?.let { angle.y = it.INTEGER().text.toIntOrNull() ?: 0 }
            vec.iaxisZ()?.let { angle.z = it.INTEGER().text.toIntOrNull() ?: 0 }
            return angle
        }
    }
}
