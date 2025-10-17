import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    groovy
    `maven-publish`
    signing
    jacoco
    idea
    id("pl.allegro.tech.build.axion-release") version "1.20.1"
    id("com.gradle.plugin-publish") version "2.0.0"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("com.coditory.integration-test") version "1.5.1"
    id("com.adarshr.test-logger") version "4.0.0"
}

scmVersion {
    unshallowRepoOnCI.set(true)
}

group = "pl.allegro.tech.build"
version = scmVersion.version

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

/**
 *  no source dirs for the java compiler
 *  compile everything in src/ with groovy
 */
sourceSets {
    main {
        java { setSrcDirs(emptyList<String>()) }
        groovy { setSrcDirs(listOf("src/main/java", "src/main/groovy")) }
    }
}

dependencies {
    api(localGroovy())
    runtimeOnly(libs.bundles.jgit.runtime)
    runtimeOnly("org.bouncycastle:bcprov-jdk18on:1.82")
    runtimeOnly("com.kohlschutter.junixsocket:junixsocket-core:2.9.1")
    runtimeOnly("net.java.dev.jna:jna-platform:5.18.1")

    implementation(libs.bundles.jgit.ssh) { exclude("com.jcraft", "jsch") }
    implementation("com.github.mwiede:jsch:0.2.24")
    implementation("com.github.zafarkhaja:java-semver:0.10.2")

    if (GradleVersion.current().version.startsWith("9.")) {
        testImplementation("org.spockframework:spock-core:2.4-M6-groovy-4.0")
        testImplementation("org.ajoberstar.grgit:grgit-core:5.3.3") {
            exclude("org.codehaus.groovy", "groovy")
        }
    } else {
        testImplementation("org.ajoberstar.grgit:grgit-core:5.3.3")
        testImplementation("org.spockframework:spock-core:2.4-M6-groovy-3.0")
    }
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.0")
    testImplementation("org.testcontainers:spock:1.21.3") {
        exclude("org.apache.commons", "commons-compress")
    }
    testImplementation("org.apache.commons:commons-compress:1.28.0")
    testImplementation("net.bytebuddy:byte-buddy:1.17.8")
    testImplementation("org.objenesis:objenesis:3.4")
    testImplementation("org.apache.sshd:sshd-core:2.16.0")
    testImplementation("org.apache.sshd:sshd-git:2.16.0")
    testImplementation("com.github.stefanbirkner:system-lambda:1.2.1")
    testImplementation(gradleTestKit())
}

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
        jvmArgs = listOf("--add-opens=java.base/java.util=ALL-UNNAMED")
    }

    /**
     * set kotlin to depend on groovy
     */
    named<KotlinCompile>("compileKotlin") {
        libraries.from(files(sourceSets.main.get().groovy.classesDirectory))
    }

    /**
     * set groovy to not depend on Kotlin
     */
    named<GroovyCompile>("compileGroovy") {
        classpath = sourceSets.main.get().compileClasspath
    }

    jacocoTestReport {
        reports {
            xml.required.set(true)
        }
        executionData(
            file("${layout.buildDirectory.asFile.get()}/jacoco/test.exec"),
            file("${layout.buildDirectory.asFile.get()}/jacoco/integrationTest.exec"),
        )
    }
}

gradlePlugin {
    website.set("https://github.com/allegro/axion-release-plugin")
    vcsUrl.set("https://github.com/allegro/axion-release-plugin")
    plugins {
        create("release") {
            id = "pl.allegro.tech.build.axion-release"
            implementationClass = "pl.allegro.tech.build.axion.release.ReleasePlugin"
            displayName = "axion-release-plugin"
            description = "Release and version management plugin."
            tags.set(listOf("git", "release", "version", "semver"))
        }
    }
    testSourceSets(sourceSets.integration.get())
}

publishing {
    afterEvaluate {
        publications {
            withType<MavenPublication> {
                pom {
                    name.set(project.name)
                    description.set("Gradle release and version management plugin")
                    url.set("https://github.com/allegro/axion-release-plugin")
                    inceptionYear.set("2014")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("adamdubiel")
                            name.set("Adam Dubiel")
                        }
                        developer {
                            id.set("bgalek")
                            name.set("Bartosz Gałek")
                        }
                    }
                    scm {
                        url.set("https://github.com/allegro/axion-release-plugin")
                        connection.set("scm:git@github.com:allegro/axion-release-plugin.git")
                        developerConnection.set("scm:git@github.com:allegro/axion-release-plugin.git")
                    }
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
        }
    }
}

signing {
    setRequired {
        System.getenv("GPG_KEY_ID") != null
    }
    useInMemoryPgpKeys(
        System.getenv("GPG_KEY_ID"),
        System.getenv("GPG_PRIVATE_KEY"),
        System.getenv("GPG_PRIVATE_KEY_PASSWORD")
    )
    sign(publishing.publications)
}

idea {
    idea {
        module {
            testSources.from(sourceSets.integration.get().allSource.srcDirs)
            testResources.from(sourceSets.integration.get().resources.srcDirs)
        }
    }
}
