import org.gradle.kotlin.dsl.sourceSets

group = "com.flooferland"
version = "1.0.3"

plugins {
    kotlin("jvm")
    id("io.kotest")
    antlr
    `maven-publish`
}

repositories {
    mavenCentral()
}

fun dep(name: String) = (project.property(name) as? String)!!
dependencies {
    // https://mvnrepository.com/artifact/org.antlr/antlr4
    antlr("org.antlr:antlr4:${dep("antlr")}")

    // JNA
    implementation("net.java.dev.jna:jna:${dep("jna")}")

    // Kotlin / Kotest
    val kotest = dep("kotest")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${dep("kotlin_serialization")}")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotest")
    testImplementation("io.kotest:kotest-framework-engine:$kotest")
    testImplementation("io.kotest:kotest-assertions-core:$kotest")
}
tasks.generateGrammarSource {
    arguments = listOf("-visitor")
    packageName = "com.flooferland.bizlib.bits.generated"
}

tasks.compileKotlin {
    dependsOn(tasks.generateGrammarSource)
}
tasks.compileTestKotlin {
    dependsOn(tasks.generateTestGrammarSource)
}

sourceSets {
    create("antlr")
    main {
        resources {
            srcDir("build/generated/resources")
        }
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

val buildBitmapFiles = tasks.register<BitmapGeneratorTask>("buildBitmapFiles") {
    description = "Builds and compiles bitmap files from CSV"
    bitmapDir.set(layout.projectDirectory.dir("src/main/resources/bitmaps"))
    bitmapsGeneratedDir.set(layout.buildDirectory.dir("generated/resources/bitmaps"))
    sourceGeneratedDir.set(layout.buildDirectory.dir("generated/kotlin/main"))
}

sourceSets.main {
    kotlin.srcDir(buildBitmapFiles.map { it.sourceGeneratedDir })
    resources.srcDir(buildBitmapFiles.map { it.bitmapsGeneratedDir })
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/FlooferLand/bizlib")
            credentials {
                username = System.getenv("USERNAME")
                password = System.getenv("PASSWORD")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}