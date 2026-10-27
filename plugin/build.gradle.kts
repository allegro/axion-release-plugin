@file:Suppress("UnstableApiUsage")

plugins {
    `kotlin-dsl`
    `java-library`
}

dependencies {
    api("com.github.zafarkhaja:java-semver:0.10.2")
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.8.0.202609011348-r")
    implementation("tools.jackson.core:jackson-databind:3.2.2")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

testing {
    suites {
        named<JvmTestSuite>("test") {
            useKotlinTest("2.3.21")

            dependencies {
                implementation(project())
            }

            targets.configureEach {
                testTask.configure {
                    inputs.files(fileTree("../examples") {
                        include("**/*.gradle.kts", "**/gradle.properties")
                        exclude("**/build/**", "**/.gradle/**")
                    }).withPropertyName("exampleBuilds").withPathSensitivity(PathSensitivity.RELATIVE)
                }
            }
        }
    }
}

gradlePlugin {
    plugins.create("tagline") {
        id = "pl.allegro.tech.build.tagline"
        implementationClass = "pl.allegro.tech.build.TaglinePlugin"
    }
}

