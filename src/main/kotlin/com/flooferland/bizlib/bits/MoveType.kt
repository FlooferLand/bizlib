package com.flooferland.bizlib.bits

enum class MoveType {
    Default,
    Servo,
    Pneumatic;

    companion object {
        fun from(text: String) = when (text) {
            "servo" -> Servo
            "pneumatic" -> Pneumatic
            else -> Default
        }
    }
}