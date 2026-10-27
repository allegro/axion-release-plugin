pluginManagement {
    repositories {
        mavenCentral()
    }
    includeBuild("../..")
}

plugins {
    id("pl.allegro.tech.build.tagline")
}

rootProject.name = "multi-module"
include("library")
