```kotlin
tagline {
    backend = [AUTO, GIT, JGIT]
    
    versioning {
        branches {
            all {
                tagPrefix = "v"
                increment = [PATCH, MINOR, MAJOR]
                tagLookup = [NEAREST_REACHABLE, HIGHEST_REACHABLE, HIGHEST_GLOBAL] // HIGHEST_GLOBAL by default
                sanitize = true
                snapshot {
                    enabled = true
                    suffix = "-SNAPSHOT"
                }
                versionCreator = [
                    VERSION,
                    VERSION_WITH_BRANCH, // default
                    VERSION_WITH_COMMIT_SHA
                ]
                versionCreator = { ... }
                release = false
            }

            default {
                versionCreator = VERSION
                snapshot {
                    enabled = false
                }
                release = true
            }

            named("release/*") {
                tagPrefix = "rv"
            }

        }
    }
    
    releasing {
        annotation {
            message = { position, version, previousVersion ->
                "Released version $version"
            }
        }
    }
    
    // fallback-adapter
    git {
        email = ""
        username = ""
        password = System.getenv("GIT_PASSWORD")
        signing = true
        key {
            path = ""
            password = ""
        }
        requireAheadOfRemote = true
    }

    gitlab {
    //?
    }
    
    github {
   //?     
    }
}
```

```kotlin
tasks {
    named("release") {
        dependsOn("generateChangelog")
        finalizedBy("generateChangelog")
    }
    
    register("generateChangelog") {
        doLast {
            ...tagline.currentVersion...
            ...tagline.nextVersion...
        }
    }
}
```

```bash
./gradlew release -Ptagline.initialVersion=0.0.1
./gradlew release -Ptagline.oldPrefix=v
./gradlew release -Ptagline.git.auth.username -Ptagline.git.auth.password
./gradlew release -Ptagline.dryRun
./gradlew release -Ptagline.localOnly
./gradlew release -Ptagline.forceVersion=1.0.0-beta.1
./gradlew release -Ptagline.forceVersion=1.0.0
```

TAGLINE_OLD_PREFIX=v
