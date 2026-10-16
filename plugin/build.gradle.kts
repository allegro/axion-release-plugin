@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
}

testing {
    suites {
        named<JvmTestSuite>("test") {
            useKotlinTest("2.3.21")

            dependencies {
                implementation(project())
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

