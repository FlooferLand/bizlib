package com.flooferland.bizlib.lsp

import org.antlr.v4.runtime.ParserRuleContext
import org.eclipse.lsp4j.DiagnosticSeverity

typealias Provider = (ctx: ParserRuleContext, doc: Document) -> ProviderValue

data class ProviderValue(
    var severity: DiagnosticSeverity = DiagnosticSeverity.Information,
    var entries: List<String>,
    var inserts: Map<String, String> = emptyMap(),
    var alsoValid: (String) -> Boolean = { false },
)