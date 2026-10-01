package com.flooferland.bizlib.bits

enum class MoveType {
    Default,
    Servo,
    Pneumatic,
    Effect;

    companion object {
        fun from(text: String) = when (text) {
            "servo" -> Servo
            "pneumatic" -> Pneumatic
            "effect" -> Effect
            else -> Default
        }
    }
}