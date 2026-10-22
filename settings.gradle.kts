@file:Suppress("LocalVariableName")

pluginManagement {
    val kotlin: String by settings
    val kotest: String by settings
    val antlr: String by settings

    plugins {
        kotlin("jvm") version (kotlin)
        id("io.kotest") version (kotest)
        antlr
        `maven-publish`
    }
}

rootProject.name = "bizlib"
include("lib", "server", "showbiz-intellij")
