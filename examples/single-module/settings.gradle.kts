import pl.allegro.tech.build.extension.Backend.AUTO
import pl.allegro.tech.build.extension.Increment.MINOR
import pl.allegro.tech.build.extension.VersionCreatorType.VERSION
import pl.allegro.tech.build.extension.VersionCreatorType.VERSION_WITH_COMMIT_SHA

pluginManagement {
    includeBuild("../..")
}

plugins {
    id("pl.allegro.tech.build.tagline")
}

rootProject.name = "single-module"

tagline {
    backend = AUTO

    versioning {
        branches {
            all {
                tagPrefix = "v"
                increment = MINOR
                useHighestVersion = true
                sanitize = true
                snapshot {
                    enabled = true
                    dirty = true
                    suffix = "-SNAPSHOT"
                }
                versionCreator = VERSION_WITH_COMMIT_SHA
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
                versionCreator { version, position ->
                    "$version-rc.${position.shortRevision}"
                }
            }
        }
    }

    releasing {
        annotation {
            message { position, version, previousVersion ->
                "Released version $version (previous: ${previousVersion ?: "none"})"
            }
        }
    }

    git {
        username = "ci-bot"
        email = "ci-bot@example.com"
        password = providers.environmentVariable("GIT_PASSWORD")
        signing = false
        requireAheadOfRemote = true
    }
}
