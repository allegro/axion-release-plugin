package pl.allegro.tech.build.axion.release.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class VerifyReleaseTask : DefaultTask() {

    init {
        group = "Release"
        description = "Verifies the project is ready to release (version is not a SNAPSHOT)."
    }

    @TaskAction
    fun verify() {
        val version = project.version.toString()
        check(!version.contains("SNAPSHOT")) {
            "[axion-release] Cannot release a SNAPSHOT version: $version"
        }
        logger.lifecycle("[axion-release] Version $version is ready for release.")
    }
}
