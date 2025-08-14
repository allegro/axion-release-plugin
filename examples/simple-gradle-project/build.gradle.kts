plugins {
    id("pl.allegro.tech.build.axion-release") version "1.14.3"
}

group = "com.example"
version = scmVersion.version

scmVersion {
}

repositories {
    mavenCentral()
}

dependencies {
    // Add your dependencies here
}
