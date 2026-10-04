package com.flooferland.bizlib.lsp

import BitsmapLexer
import BitsmapParser
import com.flooferland.bizlib.bits.BitUtils
import java.util.concurrent.CompletableFuture
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.Trees
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.TextDocumentService
import kotlin.collections.filter
import kotlin.collections.flatMap
import kotlin.collections.mapNotNull

class BitsMapTextDocumentService : TextDocumentService {
    var client: LanguageClient? = null
    val documents = mutableMapOf<String, Document>()

    val completions: Map<Int, CompletionProvider> = mapOf(
        BitsmapParser.RULE_map to { _, _ -> CompletionProviderValue(CompiledBitmaps.ids.toList()) },
        BitsmapParser.RULE_fixture to { ctx, doc -> CompletionProviderValue(doc.findFixtures(ctx, includeOld = false)) },
        BitsmapParser.RULE_bit to { ctx, doc ->
            val statement = ctx.closest<BitsmapParser.MappedMovementContext>()
            val map = statement?.map()?.text
            val movements = map?.let { doc.movements(it, includeOld = true) }.orEmpty()
            CompletionProviderValue(
                entries = movements.keys.sorted(),
                inserts = movements.mapValues { it.value.toString() }
            )
        }
    )
    val diagnosers: Map<Int, DiagnosticProvider> = mapOf(
        BitsmapParser.RULE_map to { ctx, _ ->
            val ids = CompiledBitmaps.ids.toList()
            val isAny = ctx.text == "any" && ctx.parent is BitsmapParser.MappedMovementContext
            if (ctx.text in ids || isAny) null
            else unknown("map", ctx.text, ids) to DiagnosticSeverity.Warning
        },
        BitsmapParser.RULE_fixture to { ctx, doc ->
            val valid = doc.findFixtures(ctx, includeOld = false)
            if (valid.isEmpty() || ctx.text in valid) null
            else unknown("fixture", ctx.text, valid) to DiagnosticSeverity.Error
        },
        BitsmapParser.RULE_bit to { ctx, doc ->
            when (doc.bitId(ctx.text)) {
                null -> "Name IDs are no longer supported. Use a bit number" to DiagnosticSeverity.Error
                !in 1u.toUShort()..BitUtils.DRAWER_MAX -> "Bit ID '${ctx.text}' is out of range (1 to ${BitUtils.DRAWER_MAX})." to DiagnosticSeverity.Error
                else -> null
            }
        },
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
        params.textDocument?.uri?.let { documents.remove(it) }
    }

    private fun setDocument(uri: String, content: String?) {
        if (content == null) return

        val errors = BitsMapErrorListener()
        val lexer = BitsmapLexer(CharStreams.fromString(content)).apply { removeErrorListeners(); addErrorListener(errors) }
        val parser = BitsmapParser(CommonTokenStream(lexer)).apply { removeErrorListeners(); addErrorListener(errors) }

        val document = Document(parser.file(), content)
        documents[uri] = document
        errors.diagnostics += validate(document)
        client?.publishDiagnostics(PublishDiagnosticsParams(uri, errors.diagnostics))
    }

    private fun validate(document: Document): List<Diagnostic> {
        return diagnosers.flatMap { (ruleIndex, validator) ->
            Trees.findAllRuleNodes(document.tree, ruleIndex)
                .filterIsInstance<ParserRuleContext>()
                .filter { it.start != null && it.stop != null && it.exception == null }
                .mapNotNull { ctx ->
                    val receiver = DiagnosticReceiver()
                    validator(receiver, ctx, document)?.let { (message, severity) ->
                        Diagnostic(ctx.range(), message, severity, "bitsmap")
                    }
                }
        }
    }

    override fun completion(params: CompletionParams): CompletableFuture<Either<List<CompletionItem?>?, CompletionList?>?>? {
        val document = documents[params.textDocument.uri]
            ?: return CompletableFuture.completedFuture(Either.forLeft(emptyList()))

        val suggestions = completeAt(document.text, params.position)
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

    private fun completeAt(text: String, position: Position): List<CompletionItem> {
        val isWord: (Char) -> Boolean = { it.isLetterOrDigit() || it == '-' || it == '_' }
        val offset = getOffset(text, position)
        val before = text.substring(0, offset)
        val prefix = before.takeLastWhile(isWord)
        val suffix = text.substring(offset).takeWhile(isWord)
        val probe = TreeProbe(this, before.dropLast(prefix.length))
        val replace = Range(
            Position(position.line, position.character - prefix.length),
            Position(position.line, position.character + suffix.length),
        )

        fun items(labels: List<String>, kind: CompletionItemKind, inserts: Map<String, String> = emptyMap()) = labels
            .filter { it.startsWith(prefix, ignoreCase = true) || inserts[it]?.startsWith(prefix, ignoreCase = true) == true }
            .map { label ->
                val insert = inserts[label] ?: label
                CompletionItem(label).apply {
                    this.kind = kind
                    filterText = if (insert != label) "$insert $label" else label
                    textEdit = Either.forLeft(TextEdit(replace, insert))
                    if (insert != label) detail = insert
                }
            }

        return items(probe.allowedKeywords, CompletionItemKind.Keyword) +
                probe.values(Document(probe.tree, before)).flatMap { items(it.entries, CompletionItemKind.EnumMember, it.inserts) }
    }

    override fun semanticTokensFull(params: SemanticTokensParams?): CompletableFuture<SemanticTokens?>? {
        val document = params?.textDocument?.uri?.let { documents[it] } ?: return CompletableFuture.completedFuture(null)
        val tokens = CommonTokenStream(BitsmapLexer(CharStreams.fromString(document.text))).apply { fill() }.tokens

        val data = mutableListOf<Int>()
        var linePos = 0
        var charPos = 0
        for (token in tokens.filter { it.type != BitsmapLexer.EOF }) {
            val type = BitsMapHighlightToken.get(token.text) ?: when (token.type) {
                BitsmapLexer.STRING -> BitsMapHighlightToken.String
                BitsmapLexer.INTEGER, BitsmapLexer.DECIMAL, BitsmapLexer.DRAWER_BIT -> BitsMapHighlightToken.Number
                BitsmapLexer.LINE_COMMENT -> BitsMapHighlightToken.Comment
                else -> if (BitsmapParser.VOCABULARY.getLiteralName(token.type) != null) BitsMapHighlightToken.Keyword else BitsMapHighlightToken.Property
            }

            val lineDelta = token.line - 1 - linePos
            val charDelta = if (lineDelta == 0) token.charPositionInLine - charPos else token.charPositionInLine
            data.addAll(listOf(lineDelta, charDelta, token.text?.length ?: 0, type.ordinal, 0))
            linePos = token.line - 1; charPos = token.charPositionInLine
        }
        return CompletableFuture.completedFuture(SemanticTokens(data))
    }

    override fun hover(params: HoverParams): CompletableFuture<Hover?>? {
        val document = documents[params.textDocument.uri] ?: return CompletableFuture.completedFuture(null)

        val (movement, bit) = Trees.findAllRuleNodes(document.tree, BitsmapParser.RULE_mappedMovement)
            .filterIsInstance<BitsmapParser.MappedMovementContext>()
            .firstNotNullOfOrNull { ctx -> ctx.bit()?.takeIf { params.position in it.range() }?.let { ctx to it } }
            ?: return CompletableFuture.completedFuture(null)

        val map = movement.map().text
        val movements = document.movements(map, includeOld = true)
        val id = document.bitId(bit.text) ?: movements[bit.text]
        val names = movements.filterValues { it.toInt() == id?.toInt() }.keys

        val markdown = if (names.isEmpty()) "Bit $id" else "`${names.joinToString(" / ")}` - bit $id\n\n`$map.${document.fixtures[map]}`"
        return CompletableFuture.completedFuture(Hover(MarkupContent(MarkupKind.MARKDOWN, markdown), bit.range()))
    }
}