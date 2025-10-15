group = "com.flooferland"
version = "1.0.0"

val kotest: String by properties

plugins {
    kotlin("jvm")
    id("io.kotest")
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.kotest:kotest-runner-junit5-jvm:${kotest}")
    testImplementation("io.kotest:kotest-framework-engine:${kotest}")
    testImplementation("io.kotest:kotest-assertions-core:${kotest}")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
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