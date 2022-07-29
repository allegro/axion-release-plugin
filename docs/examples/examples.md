# Usage examples

## Basic Allegro setup

This is basic Allegro setup we use in most projects. Tag prefix is set to root project name
and each version has branch name appended (unless on master). This allows us on publishing snapshots
of branches that are ready for testing:

    scmVersion {
        versionCreator 'versionWithBranch'
    }


## Update README version

This replacement pattern will update any `version* x.x.x` occurrences in README.md and create release commit:

    scmVersion {
        versionCreator 'versionWithBranch'

        hooks {
            pre 'fileUpdate', [file: 'README.md', pattern: {v,p -> /(version.) $v/}, replacement: {v, p -> "\$1 $v"}]
            pre 'commit'
        }
    }

## Use with Gradle Kotlin DSL

As of v1.13.8, configuration constructs are compatible with Gradle's Kotlin DSL.  As the configuration is richly typed, IDE code completion will be available (for IDEs that provide this for Gradle Kotlin DSL build scripts)
with these key configuration objects available for reference:
* [VersionConfig](https://github.com/allegro/axion-release-plugin/blob/main/src/main/groovy/pl/allegro/tech/build/axion/release/domain/VersionConfig.groovy)
* [TagNameSerializationConfig](https://github.com/allegro/axion-release-plugin/blob/main/src/main/groovy/pl/allegro/tech/build/axion/release/domain/TagNameSerializationConfig.groovy)
* [HooksConfig](https://github.com/allegro/axion-release-plugin/blob/main/src/main/groovy/pl/allegro/tech/build/axion/release/domain/hooks/HooksConfig.groovy)
* [RepositoryConfig](https://github.com/allegro/axion-release-plugin/blob/main/src/main/groovy/pl/allegro/tech/build/axion/release/domain/RepositoryConfig.groovy)
* [MonorepoConfig](https://github.com/allegro/axion-release-plugin/blob/main/src/main/java/pl/allegro/tech/build/axion/release/domain/MonorepoConfig.java)
* [NextVersionConfig](https://github.com/allegro/axion-release-plugin/blob/main/src/main/java/pl/allegro/tech/build/axion/release/domain/NextVersionConfig.java)
* [ChecksConfig](https://github.com/allegro/axion-release-plugin/blob/main/src/main/java/pl/allegro/tech/build/axion/release/domain/ChecksConfig.java)

A full example showing most configurable elements:

```kotlin
scmVersion {
            localOnly = true
            useHighestVersion = true
            tag {
                prefix = "release"
                versionSeparator = "/"

                // configure via function calls
                setDeserializer({ tagProperties,scmPostion,String -> "tag" })
                setSerializer( { tagProperties,version -> "tag" })

                // or, assign to existing variables
                serialize = TagProperties.Serializer({ tagProperties,version -> "tag" })
                deserialize = TagProperties.Deserializer({ tagProperties,scmPostion,String -> "tag" })
            }
            repository {
                type = "git"
            }
            checks {
                setAheadOfRemote(false)
                setSnapshotDependencies(true)
            }
            nextVersion {
                // function calls
                setDeserializer( { nextVersionProperties,scmPosition,tag -> "version" })
                setSerializer( {nextVersionProperties,version -> "version"})
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
                    commit { releaseVersion, position -> "New commit message for version $releaseVersion" }
                    custom { context -> println("$context")}
                    fileUpdate {
                        file("README.md") // repeat for additional files
                        pattern = {previousVersion,context -> "version: $previousVersion"}
                        replacement = {currentVersion,context -> "version: $currentVersion"}
                    }
                }

                postRelease {
                    push()
                    commit { releaseVersion, position -> "New commit message for version $releaseVersion" }
                    custom { context -> println("$context")}
                    fileUpdate {
                        file("README.md") // repeat for additional files
                        pattern = {previousVersion,context -> "version: $previousVersion"}
                        replacement = {currentVersion,context -> "version: $currentVersion"}

                    }
                }
            }
            monorepos {
            }

            versionCreator = VersionProperties.Creator({versionFromTag,scmPosition -> "version"})
            versionCreator({versionFromTag,scmPosition -> "version"})

            snapshotCreator = VersionProperties.Creator({versionFromTag,scmPosition -> "version"})
            snapshotCreator({versionFromTag,scmPosition -> "version"})

            versionIncrementer = VersionProperties.Incrementer({versionIncrementerContext -> Version})
            versionIncrementer({versionIncrementerContext -> Version})
        }

```
