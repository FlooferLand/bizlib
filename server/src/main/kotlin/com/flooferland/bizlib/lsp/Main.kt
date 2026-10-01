package com.flooferland.bizlib.lsp

import org.eclipse.lsp4j.launch.LSPLauncher

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val server = BitsMapLanguageServer()
        val launcher = LSPLauncher.createServerLauncher(server, System.`in`, System.out)
        server.connect(launcher.remoteProxy)
        launcher.startListening().get()
    }
}