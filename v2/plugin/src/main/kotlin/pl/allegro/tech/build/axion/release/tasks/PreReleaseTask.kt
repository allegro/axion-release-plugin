package pl.allegro.tech.build.axion.release.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.HookContext
import pl.allegro.tech.build.axion.release.ScmVersionExtension

open class PreReleaseTask : DefaultTask() {

    init {
        group = "Release"
        description = "Lifecycle hook: runs before createRelease. Add dependsOn to inject pre-release steps."
    }

    @TaskAction
    fun run() {
        val extension = project.extensions.getByType(ScmVersionExtension::class.java)
        val context = HookContext(project.version.toString(), extension.previousVersion, project)
        extension.hooks.preHooks.forEach { it(context) }
    }
}
