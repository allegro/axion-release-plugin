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

val jgitVersion: String by rootProject.extra
val jschVersion: String by rootProject.extra

dependencies {
    implementation(localGroovy())

    implementation("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:$jgitVersion") {
        exclude("com.jcraft", "jsch")
    }
    implementation("com.github.mwiede:jsch:$jschVersion")
    implementation("com.github.zafarkhaja:java-semver:0.9.0")
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release.set(8)
    }

    withType<GroovyCompile>().configureEach {
        options.release.set(8)
    }
}
