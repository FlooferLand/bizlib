package com.flooferland.bizlib.lsp

enum class BitsMapHighlightToken {
    Keyword,
    Function,
    Property,
    String,
    Boolean,
    Number,
    Comment
    ;

    companion object {
        val ids = entries.map { it.name.lowercase() }
        fun get(text: String) = when (text) {
            "set", "any", "version",
            "linear", "ease-in", "servo", "pneumatic", "effect" -> Keyword
            "flow", "move", "rotate", "anim", "wigglemul", "type", "hold" -> Function
            "x", "y", "z" -> Property
            in CompiledBitmaps.ids -> Property
            else -> null
        }
    }
}
