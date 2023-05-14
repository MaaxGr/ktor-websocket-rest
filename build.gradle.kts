import org.jetbrains.kotlin.konan.properties.Properties


val propertiesFile = Properties().apply {
    load(project.rootProject.file("local.properties").inputStream())
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.10" apply false
    kotlin("plugin.serialization") version "1.8.10" apply false
    `maven-publish`
    signing
}

configure(subprojects) {
    apply<JavaPlugin>()
    apply<MavenPublishPlugin>()
    apply<SigningPlugin>()


    configure<JavaPluginExtension> {
        withJavadocJar()
        withSourcesJar()
    }

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "OSSRH"
                setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
                credentials {
                    username = propertiesFile.getProperty("ossrh.username") ?: return@credentials
                    password = propertiesFile.getProperty("ossrh.token") ?: return@credentials
                }
            }
        }

        publications {
            register<MavenPublication>("gpr") {
                groupId = "net.grossmax.ktorwebsocketrest"
                version = "0.0.4"

                versionMapping {
                    allVariants {
                        fromResolutionResult()
                    }
                }

                from(components["java"])

                pom {
                    name.set("client")
                    description.set("Client for ktor-websocket-rest")
                    url.set("https://github.com/MaaxGr/ktor-websocket-rest")
                    licenses {
                        license {
                            name.set("MIT")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    developers {
                        developer {
                            id.set("MaaxGr")
                            name.set("Max Großmann")
                            email.set("maven@maax.gr")
                        }
                    }
                    scm {
                        connection.set("scm:git:git@github.com:MaaxGr/ktor-websocket-rest.git")
                        url.set("https://github.com/MaaxGr/ktor-websocket-rest")
                    }
                }
            }
        }
    }

    configure<SigningExtension> {
        val key = File(rootDir, "private_key.pgp").readText()
        val password = propertiesFile.getProperty("signing.password") ?: return@configure
        val publishing: PublishingExtension by project

        useInMemoryPgpKeys(key, password)
        sign(publishing.publications)
    }
}