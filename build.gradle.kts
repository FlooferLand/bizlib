group = "com.flooferland"
version = "1.0.0"

plugins {
    kotlin("jvm")
    id("io.kotest")
    `maven-publish`
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

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/FlooferLand/bizlib")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}