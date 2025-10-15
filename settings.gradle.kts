pluginManagement {
    val kotlin: String by settings
    val kotest: String by settings

    plugins {
        kotlin("jvm") version(kotlin)
        id("io.kotest") version(kotest)
        `maven-publish`
    }
}

rootProject.name = "bizlib"