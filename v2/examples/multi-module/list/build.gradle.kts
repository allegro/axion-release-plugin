plugins {
    `java-library`
    id("pl.allegro.tech.build.axion.release")
}

group = "com.example"

scmVersion {
    tag {
        prefix = "list-v"
    }
}
