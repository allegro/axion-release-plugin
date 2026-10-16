@file:Suppress("UnstableApiUsage")

plugins {
    `kotlin-dsl`
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

