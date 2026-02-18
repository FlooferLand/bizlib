import com.strumenta.antlrkotlin.gradle.AntlrKotlinTask
import org.gradle.kotlin.dsl.sourceSets

group = "com.flooferland"
version = "1.0.3"

val kotest: String by properties
val antlr: String by properties
@Suppress("PropertyName")
val kotlin_serialization: String by properties
@Suppress("PropertyName")
val antlr_kotlin: String by properties
val jna: String by properties

plugins {
    kotlin("jvm")
    id("io.kotest")
    id("com.strumenta.antlr-kotlin")
    antlr
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/org.antlr/antlr4
    antlr("org.antlr:antlr4:${antlr}")
    implementation("com.strumenta:antlr-kotlin-runtime:${antlr_kotlin}")

    // Kotlin / Kotest
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${kotlin_serialization}")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:${kotest}")
    testImplementation("io.kotest:kotest-framework-engine:${kotest}")
    testImplementation("io.kotest:kotest-assertions-core:${kotest}")

    // JNA
    implementation("net.java.dev.jna:jna:${jna}")
}

val generateKotlinGrammarSource = tasks.register<AntlrKotlinTask>("generateKotlinGrammarSource") {
    dependsOn("cleanGenerateKotlinGrammarSource")

    source = fileTree(layout.projectDirectory.dir("src/main/antlr")) {
        include("**/*.g4")
    }

    val pkgName = "com.flooferland.bizlib.bits.generated"
    packageName = pkgName

    arguments = listOf("-visitor")

    val outDir = "generatedAntlr/${pkgName.replace(".", "/")}"
    outputDirectory = layout.buildDirectory.dir(outDir).get().asFile
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
    sourceSets {
        main {
            kotlin {
                srcDir(generateKotlinGrammarSource)
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.register<BitmapGeneratorTask>("buildBitmapFiles") {
    val bitmapsDir = file("src/main/resources/bitmaps")
    val bitmapsGeneratedDir = file("build/generated/resources/bitmaps")
    val vscodeBitmapsDir = file("showbiz-vscode/data/bitmaps")

    inputs.dir(bitmapsDir)
    outputs.dir(bitmapsGeneratedDir)
    outputs.dir(vscodeBitmapsDir)

    this.bitmapDir = bitmapsDir
    this.bitmapsGeneratedDir = bitmapsGeneratedDir

    doLast {
        copy {
            from(bitmapsGeneratedDir)
            into(vscodeBitmapsDir)
            include("*.json")
        }
    }
}

tasks.processResources {
    exclude("*.dbg")
    dependsOn("buildBitmapFiles")
}
tasks.processTestResources {
    dependsOn("buildBitmapFiles")
}

tasks.build {
    dependsOn("buildBitmapFiles")
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