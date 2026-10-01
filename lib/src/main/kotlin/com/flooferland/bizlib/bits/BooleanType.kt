package com.flooferland.bizlib.bits

enum class BooleanType(var default: Boolean = false) {
    Default,
    Yes,
    No;

    fun toBoolean() = when (this) {
        Default -> default
        Yes -> true
        No -> false
    }

    companion object {
        fun defaultYes() = Default.also { it.default = true }
        fun defaultNo() = Default.also { it.default = false }
        fun from(text: String) = when (text) {
            "yes" -> Yes
            "no" -> No
            else -> Default
        }
    }
}