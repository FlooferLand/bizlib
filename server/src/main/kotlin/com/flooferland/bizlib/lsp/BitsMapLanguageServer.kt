package com.flooferland.bizlib.lsp

import java.util.concurrent.CompletableFuture
import org.eclipse.lsp4j.CompletionOptions
import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.SemanticTokensLegend
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.TextDocumentSyncKind
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageClientAware
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.WorkspaceService

class BitsMapLanguageServer : LanguageServer, LanguageClientAware {
    private val textService = BitsMapTextDocumentService()

    override fun initialize(params: InitializeParams): CompletableFuture<InitializeResult> {
        val capabilities = ServerCapabilities().apply {
            textDocumentSync = Either.forLeft(TextDocumentSyncKind.Full)
            completionProvider = CompletionOptions(false, listOf(".", " "))
            semanticTokensProvider = SemanticTokensWithRegistrationOptions().apply {
                legend = SemanticTokensLegend(BitsMapHighlightToken.ids, emptyList())
                full = Either.forLeft(true)
            }
            hoverProvider = Either.forLeft(true)
        }
        return CompletableFuture.completedFuture(InitializeResult(capabilities))
    }

    override fun exit() {}
    override fun shutdown(): CompletableFuture<in Any> {
        return CompletableFuture.completedFuture(null)
    }

    override fun getTextDocumentService() = textService
    override fun getWorkspaceService(): WorkspaceService = object : WorkspaceService {
        override fun didChangeConfiguration(params: DidChangeConfigurationParams) {}
        override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams) {}
    }

    override fun connect(client: LanguageClient) {
        textService.client = client
    }
}