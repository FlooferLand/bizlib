group = "com.flooferland"
version = "1.0.0"

plugins {
    kotlin("jvm")
    id("io.kotest")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.kotest:kotest-framework-engine:${property("kotest")}")
    testImplementation("io.kotest:kotest-assertions-core:${property("kotest")}")
}

kotlin {
    jvmToolchain(21)
}