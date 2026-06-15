import pl.allegro.tech.build.axion.release.version.VersionCreator

plugins {
    `java-library`
    id("pl.allegro.tech.build.axion.release")
}

group = "com.example"

// Appends the short commit hash to every version, e.g. "1.2.3-abc1234".
// Useful for CI artifact traceability — any published artifact can be traced back to an exact commit.
scmVersion {
    versionCreator = VersionCreator.WITH_COMMIT_HASH
}
