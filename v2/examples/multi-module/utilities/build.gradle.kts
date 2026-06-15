plugins {
    `java-library`
    id("pl.allegro.tech.build.axion.release")
}

group = "com.example"

dependencies {
    api(project(":list"))
}

scmVersion {
    tag {
        prefix = "utils-v"
    }
}
