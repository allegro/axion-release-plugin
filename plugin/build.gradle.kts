plugins {
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.zafarkhaja:java-semver:0.10.2")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.10.0.202406032230-r")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter("5.10.2")
        }
        val integrationTest by registering(JvmTestSuite::class) {
            dependencies {
                implementation(project())
            }
            targets {
                all {
                    testTask.configure { shouldRunAfter(test) }
                }
            }
        }
    }
}

gradlePlugin {
    val greeting by plugins.creating {
        id = "pl.allegro.tech.build.release"
        implementationClass = "pl.allegro.tech.build.AxionReleasePlugin"
    }
}

gradlePlugin.testSourceSets.add(sourceSets["integrationTest"])

tasks.named<Task>("check") {
    dependsOn(testing.suites.named("integrationTest"))
}
