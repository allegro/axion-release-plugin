@file:Suppress("UnstableApiUsage")

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("com.github.zafarkhaja:java-semver:0.10.2")
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.8.0.202609011348-r")
    implementation("com.squareup.moshi:moshi:1.15.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
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

