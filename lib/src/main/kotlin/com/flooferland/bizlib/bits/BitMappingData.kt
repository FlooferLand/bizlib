package com.flooferland.bizlib.bits

data class BitMappingData(
    val flow: FlowCommand?,
    val oldFlow: OldFlowCommand?,
    val name: String? = null,

    val rotates: List<RotateCommand> = listOf(),
    val moves: List<MoveCommand> = listOf(),
    val anim: List<AnimCommand> = listOf(),
    val type: MoveType,
    val hold: BooleanType,

    val wiggleMul: Double
)

enum class Easing {
    Default,
    Smooth,
    Linear,
    EaseIn;

    companion object {
        fun from(text: String) = when (text) {
            "smooth" -> Smooth
            "linear" -> Linear
            "ease-in" -> EaseIn
            else -> Default
        }
    }
}

data class FlowCommand(
    val speedIn: Double?,
    val speedOut: Double?,
    val gravityIn: Double?,
    val gravityOut: Double?,
    val slamIn: Double?,
    val slamOut: Double?,
    val slamInSpeed: Double?,
    val slamOutSpeed: Double?,
    val easing: Easing = Easing.Default
)

data class OldFlowCommand(
    val speed: Double = 1.0,
    val easing: Easing = Easing.Default
)

data class AnimCommand(
    val id: String? = null,
    val on: String? = null,
    val off: String? = null
)

data class RotateCommand(
    val bone: String,
    val target: Coords3
)

data class MoveCommand(
    val bone: String,
    val target: Coords3
)

data class Coords3(var x: Double = 0.0, var y: Double = 0.0, var z: Double = 0.0) {
    constructor(x: Double?, y: Double?, z: Double?) : this(x ?: 0.0, y ?: 0.0, z ?: 0.0)
}
