package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir

class KotlinDslCompatibilityTests extends Specification {
    @TempDir
    File temporaryFolder

    def "should not fail build when Kotlin DSL used to configure Axion"() {
        given:
        new FileTreeBuilder(temporaryFolder).file("build.gradle.kts",
            """
        import pl.allegro.tech.build.axion.release.domain.*
        import pl.allegro.tech.build.axion.release.domain.properties.*

        plugins {
            id("pl.allegro.tech.build.axion-release")
        }

        scmVersion {
            localOnly.set(true)
            useHighestVersion.set(true)
            tag {
                prefix.set("release")
                versionSeparator.set("/")

                // configure via function calls
                deserializer({ a,b,c -> "d" })
                serializer( { a,b -> "c" })

                // assignment to existing properties
                serialize.set(TagProperties.Serializer({a,b -> "c"}))
                deserialize.set(TagProperties.Deserializer({a,b,c -> "d"}))
            }
            repository {
                type.set("git")
            }
            checks {
                aheadOfRemote.set(false)
                snapshotDependencies.set(true)
            }
            nextVersion {
                // function calls
                deserializer( { a,b,c -> "d" })
                serializer( {a,b -> "c"})
            }
            hooks {
                pre({println("here")})
                pre("commit") {
                  println("here")
                }

                post({println("here")})
                post("commit") {
                  println("here")
                }

                preRelease {
                    push()
                    commit { releaseVersion, position -> "New commit message for version \$releaseVersion" }
                    custom { context -> println("\$context")}
                    fileUpdate {
                        file("README.md") // repeat for additional files
                        pattern = {previousVersion,context -> "version: \$previousVersion"}
                        replacement = {currentVersion,context -> "version: \$currentVersion"}
                    }
                }

                postRelease {
                    push()
                    commit { releaseVersion, position -> "New commit message for version \$releaseVersion" }
                    custom { context -> println("\$context")}
                    fileUpdate {
                        file("README.md") // repeat for additional files
                        pattern = {previousVersion,context -> "version: \$previousVersion"}
                        replacement = {currentVersion,context -> "version: \$currentVersion"}

                    }
                }
            }
            monorepos {
            }

            versionCreator(VersionProperties.Creator({a,b -> "c"}))
            versionCreator({a,b -> "c"})

            snapshotCreator(VersionProperties.Creator({a,b -> "c"}))
            snapshotCreator({a,b -> "c"})

            versionIncrementer(VersionProperties.Incrementer({a -> null}))
            versionIncrementer({a -> null})
        }

        project.version = scmVersion.version
        """)

        when:
        def result = GradleRunner.create()
            .withProjectDir(temporaryFolder)
            .withPluginClasspath()
            .withArguments('currentVersion')
            .build()

        then:
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }
}
