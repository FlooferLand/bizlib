package com.flooferland.bizlib.lsp

import com.flooferland.bizlib.bits.BitUtils
import com.flooferland.bizlib.bits.BitsMap
import com.flooferland.bizlib.bits.ParamSet
import com.flooferland.bizlib.bits.generated.BitsmapLexer
import com.flooferland.bizlib.bits.generated.BitsmapParser
import java.util.concurrent.CompletableFuture
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.Trees
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.TextDocumentService
import kotlin.collections.filter
import kotlin.collections.flatMap
import kotlin.collections.mapValues

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

        val analysis = BitsMap().analyze(content)
        documents[uri] = Document(analysis.tree, content)
        val diagnostics = analysis.messages.map { message ->
            val pos = message.position
            Diagnostic(
                pos?.let { Range(Position(it.start.line - 1, it.start.column), Position(it.end.line - 1, it.end.column)) } ?: Range(Position(0, 0), Position(0, 0)),
                message.text,
                if (message.severity == BitsMap.Severity.Error) DiagnosticSeverity.Error else DiagnosticSeverity.Warning,
                "bitsmap"
            )
        }
        client?.publishDiagnostics(PublishDiagnosticsParams(uri, diagnostics))
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

        val fieldName = probe.tree.getFieldName(probe.caretLine)
        if (fieldName != null) run {
            val params = ParamSet.registry[fieldName]?.params ?: return@run
            val words = before.dropLast(prefix.length).substringAfterLast('\n').split(' ')

            val targets = params.filterKeys { it.target }.values.flatMap { it.completionResults }
            if (targets.isNotEmpty() && targets.none { it in words }) {
                return items(targets, CompletionItemKind.Keyword)
            }

            val names = params.keys.mapNotNull { it.name }.filter { name -> words.none { it.startsWith("$name:") } }
            return items(names, CompletionItemKind.Keyword, names.associateWith { "$it:" })
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

        val mappedMovement = Trees.findAllRuleNodes(document.tree, BitsmapParser.RULE_mappedMovement)
            .filterIsInstance<BitsmapParser.MappedMovementContext>()
            .firstNotNullOfOrNull { ctx -> ctx.takeIf { params.position in it.range() }?.let { ctx to it.bit() } }
        if (mappedMovement != null) {
            val (movement, bit) = mappedMovement
            val map = movement.map().text
            val movements = document.movements(map, includeOld = true)
            val id = BitUtils.parseBitId(bit.text) ?: movements[bit.text]
            val names = movements.filterValues { it.toInt() == id?.toInt() }.keys

            val value = if (names.isEmpty()) "Bit $id" else "`${names.joinToString(" / ")}` - bit $id\n\n`$map.${document.fixtures[map]}`"
            return CompletableFuture.completedFuture(Hover(MarkupContent(MarkupKind.MARKDOWN, value), bit.range()))
        }

        /*val command = Trees.findAllRuleNodes(document.tree, BitsmapParser.RULE_bitField)
            .filterIsInstance<BitsmapParser.BitFieldContext>()
            .firstNotNullOfOrNull { ctx -> ctx.takeIf { params.position in (it.getChild(0) as ParserRuleContext).range() }}
        if (command != null) {
            val value = when {
                command.flowField() != null -> "Air flow controls"
                else -> "Command"
            }
            return CompletableFuture.completedFuture(Hover(MarkupContent(MarkupKind.MARKDOWN, value), command.range()))
        }*/

        return CompletableFuture.completedFuture(null)
    }

    override fun inlayHint(params: InlayHintParams): CompletableFuture<List<InlayHint>> {
        val document = documents[params.textDocument.uri] ?: return CompletableFuture.completedFuture(emptyList())
        val lines = document.text.lines()
        val hints = Trees.findAllRuleNodes(document.tree, BitsmapParser.RULE_bitStmt)
            .filterIsInstance<BitsmapParser.BitStmtContext>()
            .mapNotNull { stmt ->
                val line = stmt.start.line - 1
                if (line !in params.range.start.line..params.range.end.line) return@mapNotNull null
                val label = stmt.mappedMovement().mapNotNull { movement ->
                    val map = movement.map().text
                    val movements = document.movements(map, includeOld = true)
                    val id = BitUtils.parseBitId(movement.bit().text) ?: movements[movement.bit().text]
                    movements.filterValues { it.toInt() == id?.toInt() }.keys.firstOrNull()
                }.distinct().joinToString(", ")
                if (label.isNotEmpty() && '#' !in lines[line]) {
                    InlayHint(Position(line, lines[line].length), Either.forLeft(label)).apply {
                        paddingLeft = true
                    }
                } else null
            }
        return CompletableFuture.completedFuture(hints)
    }

    override fun onTypeFormatting(params: DocumentOnTypeFormattingParams): CompletableFuture<List<TextEdit>> {
        val document = documents[params.textDocument.uri] ?: return CompletableFuture.completedFuture(emptyList())
        val position = params.position
        if (params.ch != "\n") return CompletableFuture.completedFuture(emptyList())

        val lines = document.text.lines()
        val prevLine = if (position.line > 0) lines[position.line - 1] else ""
        val trimmedPrev = prevLine.trim()

        val insideBlock = Trees.findAllRuleNodes(document.tree, BitsmapParser.RULE_bitStmt)
            .filterIsInstance<BitsmapParser.BitStmtContext>()
            .any { position in it.range() }

        if (!insideBlock && !trimmedPrev.endsWith("{")) {
            return CompletableFuture.completedFuture(emptyList())
        }

        val indent = if (params.options.isInsertSpaces) " ".repeat(params.options.tabSize) else "\t"
        val prevIndent = prevLine.takeWhile { it == ' ' || it == '\t' }
        val targetIndent = if (trimmedPrev.endsWith("{")) prevIndent + indent else prevIndent

        val lineStart = Position(position.line, 0)
        return CompletableFuture.completedFuture(listOf(
            TextEdit(Range(lineStart, position), targetIndent)
        ))
    }
}