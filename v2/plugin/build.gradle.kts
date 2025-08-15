plugins {
    `java-gradle-plugin`
    jacoco
    alias(libs.plugins.kotlin.jvm)
    id("com.adarshr.test-logger") version "4.0.0"
}

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.3.0.202506031305-r")
    implementation("com.github.zafarkhaja:java-semver:0.10.2")
    implementation("com.github.slugify:slugify:3.0.7")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.mockito:mockito-core:5.+")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

gradlePlugin {
    val axionRelease by plugins.creating {
        id = "pl.allegro.tech.build.axion.release"
        implementationClass = "pl.allegro.tech.build.axion.release.AxionReleasePlugin"
    }
    website.set("https://github.com/allegro/axion-release-plugin")
    vcsUrl.set("https://github.com/allegro/axion-release-plugin")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
