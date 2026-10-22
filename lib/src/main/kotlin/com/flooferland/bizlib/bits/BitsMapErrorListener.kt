package com.flooferland.bizlib.bits

import com.flooferland.bizlib.bits.generated.BitsmapParser
import org.antlr.v4.runtime.*

class BitsMapErrorListener(private val raw: String, private val messages: MutableList<BitsMap.LogMessage>) : BaseErrorListener() {
    override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int, charPositionInLine: Int, msg: String?, e: RecognitionException?) {
        val parser = recognizer as? Parser
        val token = offendingSymbol as? Token

        val message = when {
            // Enforcing xyz ordering
            token?.text in setOf("x", "y", "z") && parser?.context is BitsmapParser.BitStmtContext -> "Coordinates must be in the following order: x, y, z"
            else -> msg ?: "Syntax error"
        }
        val pos = BitsMap.LogMessage.Pos(
            BitsMap.LogMessage.Point(line, charPositionInLine),
            BitsMap.LogMessage.Point(line, charPositionInLine + (token?.text?.length ?: 1))
        )
        messages += BitsMap.LogMessage(message, position = pos, context = pos.contextAround(raw), severity = BitsMap.Severity.Warning)
    }
}