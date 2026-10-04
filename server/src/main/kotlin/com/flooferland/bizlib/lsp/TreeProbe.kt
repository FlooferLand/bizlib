package com.flooferland.bizlib.lsp

import BitsmapLexer
import BitsmapParser
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.misc.IntervalSet
import kotlin.collections.orEmpty

class TreeProbe(val service: BitsMapTextDocumentService, beforeCaret: String) {
    private val caretLine = beforeCaret.count { it == '\n' } + 1
    private val parser = BitsmapParser(
        CommonTokenStream(BitsmapLexer(CharStreams.fromString(beforeCaret + "\u0001")).apply { removeErrorListeners() })
    ).apply { removeErrorListeners() }

    private var expected: IntervalSet? = null
    private var errorRule: ParserRuleContext? = null
    val tree: ParserRuleContext

    private val finished by lazy {
        generateSequence(tree) { it.lastRule }.last().parents()
            .takeWhile { it.isComplete }
            .map { it to it.followers(parser.atn) }
            .toList()
    }
    private val inStatement by lazy { finished.firstOrNull()?.first?.stop?.line == caretLine }
    private val candidates by lazy {
        if (inStatement) finished.filter { !it.second.isEmpty }.take(1) else finished
    }

    val allowedKeywords: List<String>
        get() {
            val types = IntervalSet()
            candidates.forEach { types.addAll(it.second.tokens) }
            if (!(inStatement && candidates.isNotEmpty())) expected?.let { types.addAll(it) }
            return types.toList()
                .mapNotNull { parser.vocabulary.getLiteralName(it)?.trim('\'') }
                .filter { it.first().isLetter() }
        }

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
        tree = parser.file()
    }

    /** Looks at what rule failed first and searches through */
    fun values(doc: Document): List<CompletionProviderValue> {
        val lookups = errorRule?.parents().orEmpty().map { setOf(it.ruleIndex) to it } +
                candidates.map { (ctx, next) -> next.rules to ctx }
        return lookups
            .map { (rules, ctx) -> rules.mapNotNull { service.completions[it]?.invoke(ctx, doc) }.filter { it.entries.isNotEmpty() } }
            .firstOrNull { it.isNotEmpty() }
            .orEmpty()
    }
}