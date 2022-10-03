plugins {
    `kotlin-dsl`
    groovy
    `maven-publish`
    signing
    jacoco
    id("pl.allegro.tech.build.axion-release") version "1.14.2"
    id("com.github.kt3k.coveralls") version "2.12.0"
    id("com.gradle.plugin-publish") version "0.21.0"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("com.coditory.integration-test") version "1.4.4"
    id("com.adarshr.test-logger") version "3.0.0"
}

scmVersion {
    versionCreator("versionWithBranch")
}

group = "pl.allegro.tech.build"
version = scmVersion.version

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

repositories {
    mavenCentral()
    maven {
        name = "ajoberstar-backup"
        url = uri("https://ajoberstar.org/bintray-backup/")
    }
}

object Versions {
    const val jgit = "5.13.1.202206130422-r"
    const val jsch = "0.1.55"
    const val jschAgent = "0.0.9"
}

/**
 *  no source dirs for the java compiler
 *  compile everything in src/ with groovy
 */
sourceSets {
    main {
        java { setSrcDirs(emptyList<String>()) }
        withConvention(GroovySourceSet::class) {
            groovy.setSrcDirs(listOf("src/main/java", "src/main/groovy"))
        }
    }
}

dependencies {
    api(localGroovy())

    runtimeOnly("org.eclipse.jgit:org.eclipse.jgit.ssh.apache:${Versions.jgit}")
    runtimeOnly("org.eclipse.jgit:org.eclipse.jgit.ui:${Versions.jgit}")
    runtimeOnly("org.eclipse.jgit:org.eclipse.jgit.gpg.bc:${Versions.jgit}")

    implementation("org.eclipse.jgit:org.eclipse.jgit:${Versions.jgit}")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:${Versions.jgit}")
    implementation("com.jcraft:jsch:${Versions.jsch}")
    implementation("com.jcraft:jsch.agentproxy.core:${Versions.jschAgent}")
    implementation("com.jcraft:jsch.agentproxy.jsch:${Versions.jschAgent}")
    implementation("com.jcraft:jsch.agentproxy.sshagent:${Versions.jschAgent}")
    implementation("com.jcraft:jsch.agentproxy.pageant:${Versions.jschAgent}")
    implementation("com.jcraft:jsch.agentproxy.usocket-jna:${Versions.jschAgent}")
    implementation("com.jcraft:jsch.agentproxy.usocket-nc:${Versions.jschAgent}")
    implementation("com.github.zafarkhaja:java-semver:0.9.0")

    testImplementation("org.ajoberstar.grgit:grgit-core:4.1.0") {
        exclude("org.eclipse.jgit", "org.eclipse.jgit.ui")
        exclude("org.eclipse.jgit", "org.eclipse.jgit")
    }
    testImplementation("org.testcontainers:spock:1.17.4")
    testImplementation("org.spockframework:spock-core:2.2-groovy-2.5")
    testImplementation("cglib:cglib-nodep:3.3.0")
    testImplementation("org.objenesis:objenesis:3.3")
    testImplementation("org.apache.sshd:sshd-core:2.9.1")
    testImplementation("org.apache.sshd:sshd-git:2.9.1")
    testImplementation(gradleTestKit())
}

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
    }

    named("check") {
        dependsOn(named("test"))
        dependsOn(named("integrationTest"))
    }

    /**
     * set kotlin to depend on groovy
     */
    named<AbstractCompile>("compileKotlin") {
        classpath += files(sourceSets.main.get().withConvention(GroovySourceSet::class) { groovy }.classesDirectory)
    }

    /**
     * set groovy to not depend on Kotlin
     */
    named<AbstractCompile>("compileGroovy") {
        classpath = sourceSets.main.get().compileClasspath
    }

    jacocoTestReport {
        reports {
            xml.required.set(true)
        }
    }
}

jacoco {
    toolVersion = "0.8.2"
}

gradlePlugin {
    testSourceSets(sourceSets.integration.get())
    plugins {
        create("release") {
            id = "pl.allegro.tech.build.axion-release"
            displayName = "axion-release-plugin"
            implementationClass = "pl.allegro.tech.build.axion.release.ReleasePlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/allegro/axion-release-plugin"
    vcsUrl = "https://github.com/allegro/axion-release-plugin"
    description = "Release and version management plugin."
    tags = listOf("release", "version")
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
        }
    }
}

if (System.getenv("GPG_KEY_ID") != null) {
    signing {
        useInMemoryPgpKeys(
            System.getenv("GPG_KEY_ID"),
            System.getenv("GPG_PRIVATE_KEY"),
            System.getenv("GPG_PRIVATE_KEY_PASSWORD")
        )
        sign(publishing.publications)
    }
}
