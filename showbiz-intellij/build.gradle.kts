plugins {
    kotlin("jvm")
    id("org.jetbrains.intellij.platform") version "2.19.0"
}

group = "com.flooferland"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    implementation(project(":server"))

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        intellijIdea("2026.1")

        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")

        plugin("com.redhat.devtools.lsp4ij", "0.19.1")
    }
}
