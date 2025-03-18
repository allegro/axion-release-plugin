plugins {
    `kotlin-dsl`
    groovy
}

/**
 *  no source dirs for the java compiler
 *  compile everything in src/ with groovy
 */
sourceSets {
    main {
        java { setSrcDirs(emptyList<String>()) }
        groovy {
            setSrcDirs(listOf("src/main/java", "src/main/groovy"))
        }
    }
}

val jgitVersion = "6.10.0.202406032230-r"
val jschVersion = "0.2.24"

dependencies {
    implementation(localGroovy())

    runtimeOnly("org.eclipse.jgit:org.eclipse.jgit.ssh.apache:$jgitVersion")
    runtimeOnly("org.eclipse.jgit:org.eclipse.jgit.ui:$jgitVersion")
    runtimeOnly("org.eclipse.jgit:org.eclipse.jgit.gpg.bc:$jgitVersion")

    implementation("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:$jgitVersion") {
        exclude("com.jcraft", "jsch")
    }
    implementation("com.github.mwiede:jsch:$jschVersion")
    api("com.github.zafarkhaja:java-semver:0.9.0")
    runtimeOnly("org.bouncycastle:bcprov-jdk18on:1.80")
    runtimeOnly("com.kohlschutter.junixsocket:junixsocket-core:2.9.1")
    runtimeOnly("net.java.dev.jna:jna-platform:5.16.0")
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release.set(8)
    }

    withType<GroovyCompile>().configureEach {
        options.release.set(8)
    }
}
