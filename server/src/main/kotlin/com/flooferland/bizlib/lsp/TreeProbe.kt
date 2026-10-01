package com.flooferland.bizlib.lsp

import BitsmapLexer
import BitsmapParser
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.misc.IntervalSet
import kotlin.collections.orEmpty

class Probe(val service: BitsMapTextDocumentService, beforeCaret: String) {
    private val caretLine = beforeCaret.count { it == '\n' } + 1
    private val parser = BitsmapParser(
        CommonTokenStream(BitsmapLexer(CharStreams.fromString(beforeCaret + "\u0001")).apply { removeErrorListeners() })
    ).apply { removeErrorListeners() }

    private var expected: IntervalSet? = null
    private var errorRule: ParserRuleContext? = null

    val tree: ParserRuleContext = parser.file()
    val allowedKeywords: List<String>
        get() = expected?.toList().orEmpty()
            .mapNotNull { parser.vocabulary.getLiteralName(it)?.trim('\'') }
            .filter { it.first().isLetter() }

    init {
        parser.addErrorListener(object : BaseErrorListener() {
            override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int, charPositionInLine: Int, msg: String?, e: RecognitionException?) {
                val tokenType = (offendingSymbol as? Token)?.type
                if (expected == null && tokenType == BitsmapParser.CARET) {
                    expected = parser.expectedTokens
                    errorRule = parser.context
                }
            }
        })
    }

    /** Looks at what rule failed first and searches through */
    fun values(doc: Document): List<String> {
        val finished = generateSequence(tree) { it.lastRule }.last().parents().toList()
        val sameLine = finished.first().stop?.line == caretLine
        val lookups = errorRule?.parents().orEmpty().map { setOf(it.ruleIndex) to it } +
                finished.take(if (sameLine) 1 else finished.size).map { it.rulesAfter(parser.atn) to it }
        return lookups
            .map { (rules, ctx) -> rules.flatMap { service.providers[it]?.invoke(ctx, doc)?.entries.orEmpty() } }
            .firstOrNull { it.isNotEmpty() }
            .orEmpty()
    }
}