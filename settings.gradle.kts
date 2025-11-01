@file:Suppress("LocalVariableName")

pluginManagement {
    val kotlin: String by settings
    val kotest: String by settings
    val antlr: String by settings
    val antlr_kotlin: String by settings

    plugins {
        kotlin("jvm") version(kotlin)
        id("io.kotest") version(kotest)
        id("com.strumenta.antlr-kotlin") version(antlr_kotlin)
        antlr
        `maven-publish`
    }
}

rootProject.name = "bizlib"