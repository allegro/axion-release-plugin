plugins {
    `java-library`
    `maven-publish`
}

group = "example"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

publishing {
    publications {
        register<MavenPublication>("library") {
            from(components["java"])
        }
    }
}

tasks.register<WriteProperties>("versionDetails") {
    destinationFile = layout.buildDirectory.file("version.properties")
    property("currentVersion", tagline.currentVersion)
    property("nextVersion", tagline.nextVersion.toString())
    property("baseVersion", tagline.versionInfo.baseVersion.toString())
    property("releaseTag", tagline.versionInfo.releaseTag ?: "")
    property("revision", tagline.versionInfo.revision)
    property("branch", tagline.versionInfo.branch ?: "")
    property("snapshot", tagline.versionInfo.snapshot)
    property("projectVersion", project.version.toString())
}

