package pl.allegro.tech.build.axion.release.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.git.ci.CiDetector
import pl.allegro.tech.build.axion.release.git.ci.EnvironmentSource

open class PostPushReleaseTask : DefaultTask() {

    init {
        group = "Release"
        description = "Lifecycle hook: runs after pushRelease. Add dependsOn to inject post-push steps (e.g. deploy, notify)."
    }

    @TaskAction
    fun run() {
        val env = EnvironmentSource.SYSTEM
        CiDetector.detect(env)?.notifyRelease(project.version.toString(), env)
        logger.lifecycle("[axion-release] Released version ${project.version}")
    }
}
