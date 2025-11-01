package com.flooferland.bizlib.bits

import kotlinx.serialization.Serializable

@Serializable
data class BitMappingData(
    val flow: Double,
    val name: String? = null,

    val rotate: RotateCommand? = null,
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
    val target: Angle3
)

@Serializable
data class Angle3(
    var x: Int = 0,
    var y: Int = 0,
    var z: Int = 0
)
