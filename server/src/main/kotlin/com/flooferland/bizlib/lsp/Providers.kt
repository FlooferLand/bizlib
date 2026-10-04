package com.flooferland.bizlib.lsp

import org.antlr.v4.runtime.ParserRuleContext
import org.eclipse.lsp4j.DiagnosticSeverity

typealias CompletionProvider = (ctx: ParserRuleContext, doc: Document) -> CompletionProviderValue
data class CompletionProviderValue(
    var entries: List<String>,
    var inserts: Map<String, String> = emptyMap()
)

typealias DiagnosticProvider = DiagnosticReceiver.(ctx: ParserRuleContext, doc: Document) -> DiagnosticProviderValue?
class DiagnosticReceiver {
    fun unknown(type: String, text: String, valid: List<String>) =
        "Unknown $type '$text'." + if (valid.size <= 2) " Expected one of: ${valid.joinToString()}." else ""
}
typealias DiagnosticProviderValue = Pair<String, DiagnosticSeverity>
