package com.flooferland.bizlib.lsp

enum class BitsMapHighlightToken {
    Keyword,
    Property,
    String,
    Variable,
    Comment,
    Number,
    Boolean
    ;

    companion object {
        val ids = entries.map { it.name.lowercase() }
        fun get(text: String) = when (text) {
            "set" -> Keyword
            "any" -> Keyword
            "version" -> Keyword
            "hold" -> Property
            in arrayOf("flow", "move", "rotate", "anim", "wigglemul", "type") -> Variable
            in arrayOf("x", "y", "z") -> Variable
            in arrayOf("linear", "ease-in") -> Keyword
            in CompiledBitmaps.ids -> Property
            else -> null
        }
    }
}
