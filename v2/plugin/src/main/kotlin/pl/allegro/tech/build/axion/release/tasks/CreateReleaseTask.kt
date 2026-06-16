package pl.allegro.tech.build.axion.release.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.ScmVersionExtension
import pl.allegro.tech.build.axion.release.git

open class CreateReleaseTask : DefaultTask() {

    init {
        group = "Release"
        description = "Creates a release tag in the local repository."
    }

    @TaskAction
    fun create() {
        val extension = project.extensions.getByType(ScmVersionExtension::class.java)
        val version = project.version.toString()
        val tagName = "${extension.tag.resolvedPrefix(project)}$version"
        val dryRun = project.findProperty("release.dryRun")?.toString()?.toBoolean() ?: false

        if (dryRun) {
            logger.lifecycle("[axion-release] DRY RUN — would create tag $tagName")
        } else {
            logger.lifecycle("[axion-release] Creating tag $tagName")
            git("tag", tagName, workingDir = project.rootProject.projectDir, config = extension.repository)
            logger.lifecycle("[axion-release] Tag $tagName created.")
        }
    }
}
