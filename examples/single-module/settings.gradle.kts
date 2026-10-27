import pl.allegro.tech.build.extension.TagLookup.HIGHEST_GLOBAL

pluginManagement {
    repositories {
        mavenCentral()
    }
    includeBuild("../..")
}

plugins {
    id("pl.allegro.tech.build.tagline")
}

rootProject.name = "single-module"

tagline {
    initialVersion = "0.1.0"
    // AUTO detects CI and falls back to Git, then JGit. To force local tag lookup while keeping CI metadata:
    // backend = pl.allegro.tech.build.extension.Backend.JGIT
    // Feature branches use SNAPSHOT. The default branch omits branch and SNAPSHOT suffixes.
    versioning {
        branches {
            all {
                tagLookup = HIGHEST_GLOBAL
            }
            named("maintenance/*") {
                tagPrefix = "maintenance-v"
            }
        }
    }
}
