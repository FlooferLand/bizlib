package com.flooferland.bizlib.lsp

import org.antlr.v4.runtime.ParserRuleContext

typealias CompletionProvider = (ctx: ParserRuleContext, doc: Document) -> CompletionProviderValue
data class CompletionProviderValue(
    var entries: List<String>,
    var inserts: Map<String, String> = emptyMap()
)
