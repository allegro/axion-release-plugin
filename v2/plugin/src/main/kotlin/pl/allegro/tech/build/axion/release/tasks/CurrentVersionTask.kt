package pl.allegro.tech.build.axion.release.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class CurrentVersionTask : DefaultTask() {

    init {
        group = "Release"
        description = "Prints the current project version derived from SCM tags."
    }

    @TaskAction
    fun run() {
        val quiet = project.findProperty("release.quiet")?.toString()?.toBoolean() ?: false
        if (quiet) println(project.version) else println("Project version: ${project.version}")
    }
}
