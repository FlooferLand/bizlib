package com.flooferland.bizlib.lsp

import BitsmapParser
import org.antlr.v4.runtime.*
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range

class BitsMapErrorListener : BaseErrorListener() {
    val diagnostics = mutableListOf<Diagnostic>()

    override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int, charPositionInLine: Int, msg: String?, e: RecognitionException?) {
        val parser = recognizer as? Parser
        val token = offendingSymbol as? Token
        val range = Range(
            Position(line - 1, charPositionInLine),
            Position(line - 1, charPositionInLine + (token?.text?.length ?: 1))
        )

        val (message, severity) = when {
            // Enforcing xyz ordering
            token?.text in setOf("x", "y", "z") && parser?.context is BitsmapParser.BitStmtContext ->
                "Coordinates must be in the following order: x, y, z" to DiagnosticSeverity.Error
            parser?.context is BitsmapParser.BitContext && (parser.context as BitsmapParser.BitContext).ID() != null ->
                "Name IDs are no longer supported. Use a bit number" to DiagnosticSeverity.Error
            else ->
                (msg ?: "Syntax error") to DiagnosticSeverity.Error
        }

        diagnostics += Diagnostic(range, message, severity, "bitsmap")
    }
}