package com.flooferland.bizlib.lsp

import BitsmapLexer
import BitsmapParser
import com.flooferland.bizlib.bits.BitUtils
import java.util.concurrent.CompletableFuture
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.Trees
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.TextDocumentService
import kotlin.collections.filter
import kotlin.collections.flatMap
import kotlin.collections.mapNotNull

typealias Provider = (ctx: ParserRuleContext, doc: Document) -> ProviderValue
data class ProviderValue(var severity: DiagnosticSeverity = DiagnosticSeverity.Information, var entries: List<String>)

class BitsMapTextDocumentService : TextDocumentService {
    var client: LanguageClient? = null
    val documents = mutableMapOf<String, String>()
    val providers: Map<Int, Provider> = mapOf(
        BitsmapParser.RULE_map to { ctx, _ -> ProviderValue(
            severity = DiagnosticSeverity.Warning,
            entries = CompiledBitmaps.ids.toList()
        ) },
        BitsmapParser.RULE_fixture to { ctx, _ -> ProviderValue(
            severity = DiagnosticSeverity.Error,
            entries = ctx.closest<BitsmapParser.SetStmtContext>()?.map()?.text
                ?.let { BitUtils.readBitmap(it)?.keys?.toList() }
                .orEmpty()
        ) },
    )

    override fun didOpen(params: DidOpenTextDocumentParams) {
        setDocument(params.textDocument.uri, params.textDocument.text)
    }

    override fun didChange(params: DidChangeTextDocumentParams) {
        setDocument(params.textDocument.uri, params.contentChanges.last().text)
    }

    override fun didSave(params: DidSaveTextDocumentParams) {
        setDocument(params.textDocument.uri, params.text)
    }

    override fun didClose(params: DidCloseTextDocumentParams) {
        documents.remove(params.textDocument.uri)
    }

    private fun setDocument(uri: String, content: String?) {
        if (content == null) return
        documents[uri] = content

        val errors = BitsMapErrorListener()
        val lexer = BitsmapLexer(CharStreams.fromString(content)).apply { removeErrorListeners(); addErrorListener(errors) }
        val parser = BitsmapParser(CommonTokenStream(lexer)).apply { removeErrorListeners(); addErrorListener(errors) }

        errors.diagnostics += validate(parser.file())
        client?.publishDiagnostics(PublishDiagnosticsParams(uri, errors.diagnostics))
    }

    private fun validate(tree: ParseTree): List<Diagnostic> {
        val doc = Document(tree)
        return providers.flatMap { (ruleIndex, provider) ->
            Trees.findAllRuleNodes(tree, ruleIndex)
                .filterIsInstance<ParserRuleContext>()
                .filter { it.start != null && it.stop != null && it.exception == null }
                .mapNotNull { ctx ->
                    val result = provider(ctx, doc)
                    if (result.entries.isEmpty()) return@mapNotNull null
                    val text = ctx.text.trim('"')
                    if (text in result.entries) return@mapNotNull null

                    val hint = if (result.entries.size <= 2) " Expected one of: ${result.entries.joinToString()}." else ""
                    Diagnostic(
                        Range(
                            Position(ctx.start.line - 1, ctx.start.charPositionInLine),
                            Position(ctx.stop.line - 1, ctx.stop.charPositionInLine + ctx.stop.text.length)
                        ),
                        "Unknown ${BitsmapParser.ruleNames[ctx.ruleIndex]} '$text'.$hint",
                        result.severity, "bitsmap"
                    )
                }
        }
    }

    override fun completion(params: CompletionParams): CompletableFuture<Either<List<CompletionItem?>?, CompletionList?>?>? {
        val document = documents[params.textDocument.uri]
            ?: return CompletableFuture.completedFuture(Either.forLeft(emptyList()))

        val suggestions = completeAt(document, getOffset(document, params.position))
        return CompletableFuture.completedFuture(Either.forLeft(suggestions))
    }

    private fun getOffset(text: String, position: Position): Int {
        var lineStart = 0
        repeat(position.line) {
            val newline = text.indexOf('\n', lineStart)
            if (newline == -1) return text.length
            lineStart = newline + 1
        }
        return (lineStart + position.character).coerceAtMost(text.length)
    }

    private fun completeAt(text: String, offset: Int): List<CompletionItem> {
        val before = text.substring(0, offset)
        val prefix = before.takeLastWhile { it.isLetterOrDigit() || it == '-' || it == '_' }
        val probe = Probe(this, before.dropLast(prefix.length))

        fun buildItems(labels: List<String>, kind: CompletionItemKind) = labels
            .filter { it.startsWith(prefix, ignoreCase = true) }
            .map { CompletionItem(it).apply { this.kind = kind } }

        return buildItems(probe.allowedKeywords, CompletionItemKind.Keyword) +
                buildItems(probe.values(Document(probe.tree)), CompletionItemKind.EnumMember)
    }
}