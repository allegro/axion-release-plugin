import pl.allegro.tech.build.extension.ProjectTaglineExtension

plugins {
    `java-library`
    `maven-publish`
}

group = "example"
val tagline = extensions.getByType<ProjectTaglineExtension>()

// Capture values as task inputs during configuration; execution needs no Project access.
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

publishing {
    publications {
        register<MavenPublication>("library") {
            from(components["java"])
        }
    }
}
