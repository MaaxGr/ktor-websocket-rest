plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":modules:shared"))

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")

    implementation("io.ktor:ktor-client-core:2.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")

}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
