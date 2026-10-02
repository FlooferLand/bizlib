package com.flooferland.showbiz

import com.flooferland.bizlib.lsp.BitsMapLanguageServer
import com.intellij.openapi.project.Project
import com.redhat.devtools.lsp4ij.LanguageServerFactory
import com.redhat.devtools.lsp4ij.server.StreamConnectionProvider
import java.io.PipedInputStream
import java.io.PipedOutputStream
import org.eclipse.lsp4j.launch.LSPLauncher

class BitsmapLspFactory : LanguageServerFactory {
    override fun createConnectionProvider(project: Project): StreamConnectionProvider {
        return ShowbizConnectionProvider()
    }
}

class ShowbizConnectionProvider : StreamConnectionProvider {
    private val clientInput = PipedInputStream()
    private val clientOutput = PipedOutputStream()

    override fun start() {
        val serverInput = PipedInputStream(clientOutput)
        val serverOutput = PipedOutputStream(clientInput)

        Thread {
            val server = BitsMapLanguageServer()
            val launcher = LSPLauncher.createServerLauncher(server, serverInput, serverOutput)
            server.connect(launcher.remoteProxy)
            launcher.startListening().get()
        }.start()
    }

    override fun getInputStream() = clientInput
    override fun getOutputStream() = clientOutput

    override fun stop() {
        clientInput.close()
        clientOutput.close()
    }
}