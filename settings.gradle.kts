rootProject.name = "axion-release-plugin"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            name = "ajoberstar-backup"
            url = uri("https://ajoberstar.org/bintray-backup/")
        }
    }
}

include(
    "lib",
)
