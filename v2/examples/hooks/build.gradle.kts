plugins {
    `java-library`
    id("pl.allegro.tech.build.axion.release")
}

group = "com.example"

scmVersion {
    hooks {
        post { context ->
            val readme = context.project.file("README.md")
            readme.writeText(
                readme.readText().replace(
                    Regex("Current version: .+"),
                    "Current version: ${context.releaseVersion}"
                )
            )
        }
    }
}
