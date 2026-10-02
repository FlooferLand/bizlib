plugins {
    kotlin("jvm")
    application
    id("com.gradleup.shadow") version "9.6.1"
    id("com.strumenta.antlr-kotlin")
    antlr
}

group = "com.flooferland"
version = "1.0.3"

repositories {
    mavenCentral()
}

fun dep(name: String) = (project.property(name) as? String)!!
dependencies {
    implementation(project(":lib"))

    // https://mvnrepository.com/artifact/org.antlr/antlr4
    antlr("org.antlr:antlr4:${dep("antlr")}")
    implementation("com.strumenta:antlr-kotlin-runtime:${dep("antlr_kotlin")}")

    // LSP4J
    val ksp4j = dep("lsp4j")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:$ksp4j")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.jsonrpc:$ksp4j")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.flooferland.bizlib.lsp.Main")
}
tasks.jar {
    manifest {
        attributes("Main-Class" to application.mainClass.get())
    }
}

val copyServerJar = tasks.register<Copy>("copyServerJar") {
    description = "Copies the language server binary into the VSCode extension"
    dependsOn(tasks.shadowJar)
    from(tasks.shadowJar.get().archiveFile)
    into(rootDir.resolve("showbiz-vscode/data/bin"))
    rename { "server.jar" }
}

tasks.assemble {
    dependsOn(copyServerJar)
}
