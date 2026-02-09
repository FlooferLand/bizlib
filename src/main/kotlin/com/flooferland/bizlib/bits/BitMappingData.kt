package com.flooferland.bizlib.bits

import com.flooferland.bizlib.bits.generated.BitsmapParser
import kotlinx.serialization.Serializable

@Serializable
data class BitMappingData(
    val flow: FlowCommand,
    val wiggleMul: Double,
    val name: String? = null,

    val rotates: List<RotateCommand> = listOf(),
    val moves: List<MoveCommand> = listOf(),
    val anim: AnimCommand? = null
)

@Serializable
enum class Easing {
    Default,
    Linear,
    EaseIn
}

@Serializable
data class FlowCommand(
    val speed: Double = 1.0,
    val easing: Easing = Easing.Default
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
data class Coords3(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f) {
    companion object {
        fun fromAntlr(vec: BitsmapParser.Vec3iContext): Coords3 {
            val angle = Coords3()
            vec.iaxisX()?.let { angle.x = it.DECIMAL().text.toFloatOrNull() ?: 0f }
            vec.iaxisY()?.let { angle.y = it.DECIMAL().text.toFloatOrNull() ?: 0f }
            vec.iaxisZ()?.let { angle.z = it.DECIMAL().text.toFloatOrNull() ?: 0f }
            return angle
        }
    }
}
