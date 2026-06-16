package pl.allegro.tech.build.axion.release.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import pl.allegro.tech.build.axion.release.ScmVersionExtension
import pl.allegro.tech.build.axion.release.git

open class PushReleaseTask : DefaultTask() {

    init {
        group = "Release"
        description = "Pushes the release tag to the remote repository."
    }

    @TaskAction
    fun push() {
        val extension = project.extensions.getByType(ScmVersionExtension::class.java)
        val version = project.version.toString()
        val tagName = "${extension.tag.resolvedPrefix(project)}$version"
        val dryRun = project.findProperty("release.dryRun")?.toString()?.toBoolean() ?: false

        if (dryRun) {
            logger.lifecycle("[axion-release] DRY RUN — would push tag $tagName to ${extension.repository.remote}")
        } else {
            logger.lifecycle("[axion-release] Pushing tag $tagName to ${extension.repository.remote}")
            git("push", extension.repository.remote, tagName,
                workingDir = project.rootProject.projectDir,
                config = extension.repository)
        }
    }
}
