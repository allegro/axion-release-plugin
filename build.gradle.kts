plugins {
    `kotlin-dsl`
    groovy
    `maven-publish`
    signing
    jacoco
    idea
    id("pl.allegro.tech.build.axion-release") version "1.18.15"
    id("com.gradle.plugin-publish") version "1.3.0"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("com.coditory.integration-test") version "2.1.0"
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
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

repositories {
    mavenCentral()
    maven {
        name = "ajoberstar-backup"
        url = uri("https://ajoberstar.org/bintray-backup/")
    }
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

val jgitVersion = "6.10.0.202406032230-r"
val jschVersion = "0.2.21"
val jschAgentVersion = "0.0.9"

dependencies {
    api(localGroovy())

    runtimeOnly("org.eclipse.jgit:org.eclipse.jgit.ssh.apache:$jgitVersion")
    runtimeOnly("org.eclipse.jgit:org.eclipse.jgit.ui:$jgitVersion")
    runtimeOnly("org.eclipse.jgit:org.eclipse.jgit.gpg.bc:$jgitVersion")

    implementation("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:$jgitVersion") {
        exclude("com.jcraft", "jsch")
    }
    implementation("com.github.mwiede:jsch:$jschVersion")
    implementation("com.github.zafarkhaja:java-semver:0.9.0")
    runtimeOnly("org.bouncycastle:bcprov-jdk18on:1.79")
    runtimeOnly("com.kohlschutter.junixsocket:junixsocket-core:2.9.1")
    runtimeOnly("net.java.dev.jna:jna-platform:5.15.0")

    testImplementation("org.ajoberstar.grgit:grgit-core:5.3.0") {
        exclude("org.eclipse.jgit", "org.eclipse.jgit.ui")
        exclude("org.eclipse.jgit", "org.eclipse.jgit")
    }
    testImplementation("org.testcontainers:spock:1.17.6")
    testImplementation("org.spockframework:spock-core:2.3-groovy-3.0")
    testImplementation("org.spockframework:spock-junit4:2.3-groovy-3.0")
    testImplementation("cglib:cglib-nodep:3.3.0")
    testImplementation("org.objenesis:objenesis:3.4")
    testImplementation("org.apache.sshd:sshd-core:2.14.0")
    testImplementation("org.apache.sshd:sshd-git:2.14.0")
    testImplementation("com.github.stefanbirkner:system-rules:1.19.0")
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
        executionData(
            file("${layout.buildDirectory.asFile.get()}/jacoco/test.exec"),
            file("${layout.buildDirectory.asFile.get()}/jacoco/integrationTest.exec"),
        )
    }
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
    module {
        testSourceDirs = testSourceDirs + sourceSets.integration.get().allSource.srcDirs
        testResourceDirs = testResourceDirs + sourceSets.integration.get().resources.srcDirs
    }
}
