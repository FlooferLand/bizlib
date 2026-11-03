pluginManagement {
    val kotlin = providers.gradleProperty("kotlin").get()
    val kotest = providers.gradleProperty("kotest").get()

    plugins {
        kotlin("jvm") version (kotlin)
        id("io.kotest") version (kotest)
    }
}

rootProject.name = "bizlib"
include("lib", "server", "showbiz-intellij")
