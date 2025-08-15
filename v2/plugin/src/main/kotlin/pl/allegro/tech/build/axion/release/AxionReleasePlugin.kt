package pl.allegro.tech.build.axion.release

import com.github.slugify.Slugify
import com.github.zafarkhaja.semver.Version
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Constants.R_TAGS
import org.eclipse.jgit.lib.Ref
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import java.io.File

private const val DEFAULT_TAG_PREFIX = "v"

class AxionReleasePlugin : Plugin<Project> {

    private val logger = Logging.getLogger(AxionReleasePlugin::class.java)
    private val slugify = Slugify.builder().build()

    override fun apply(project: Project) {
        openGitRepository(project)?.let { git ->
            val runtimeEnvironment = scanEnvironment(git)
            logger.lifecycle("[axion-release] Current branch: ${runtimeEnvironment.branchName}")

            if (runtimeEnvironment.isOnReleaseBranch) {
                logger.lifecycle("[axion-release] Detected a release branch: ${runtimeEnvironment.branchName}, release will be a RELEASE version")
            } else {
                logger.lifecycle("[axion-release] You are not on a release branch: ${runtimeEnvironment.branchName}, project will be a SNAPSHOT version")
            }

            if (runtimeEnvironment.isShallow) logger.info("Shallow clone detected")
            if (runtimeEnvironment.hasUnreleasedChanges) logger.lifecycle("[axion-release] Unreleased changes detected")

            if (runtimeEnvironment.refsByPrefix.isEmpty()) {
                logger.lifecycle("[axion-release] No tags found. Setting project to initial 0.0.1 version")
                val currentVersion = Version.of(0, 0, 0)
                if (runtimeEnvironment.isOnReleaseBranch) {
                    project.version = currentVersion.nextPatchVersion()
                } else {
                    project.version =
                        currentVersion.nextPatchVersion("${slugify.slugify(runtimeEnvironment.branchName)}-SNAPSHOT")
                }
            }

            //TODO: do not iterate over all to sort, probably git describe can be used
            runtimeEnvironment.refsByPrefix.mapNotNull { tag ->
                val versionString = tag.name.removePrefix(R_TAGS).removePrefix(DEFAULT_TAG_PREFIX)
                Version.tryParse(versionString)?.let { Pair(it.get(), tag) }
            }.maxByOrNull { it.first }?.let { pair ->
                val version = pair.first
                val tag = pair.second
                logger.lifecycle("[axion-release] Found tag: ${tag.name}, it will be used to calculate next project version")
                if (runtimeEnvironment.isOnReleaseBranch && runtimeEnvironment.hasUnreleasedChanges) {
                    project.version = version.nextPatchVersion()
                } else if (!runtimeEnvironment.isOnReleaseBranch && runtimeEnvironment.hasUnreleasedChanges) {
                    project.version =
                        version.nextPatchVersion("${slugify.slugify(runtimeEnvironment.branchName)}-SNAPSHOT")
                } else {
                    project.version = version
                }
            }

        }

        project.tasks.register("release") { task ->
            task.doLast {
                println("Hello from `release` task of 'pl.allegro.tech.build.axion-release' plugin")
            }
        }
    }

    private fun scanEnvironment(git: Git): RuntimeEnvironment {
        val shallowFile = File(git.repository.directory, "shallow")
        val branchName = git.repository.branch

        // current HEAD check
        val head = git.repository.resolve(Constants.HEAD)
        val taggedCommits = git.tagList().setContains(head).call()
        val refsByPrefix = git.repository.refDatabase.getRefsByPrefix("$R_TAGS$DEFAULT_TAG_PREFIX")
        val hasUnreleasedChanges = !refsByPrefix.isEmpty() && !taggedCommits.contains(refsByPrefix[0])

        return RuntimeEnvironment(
            branchName,
            shallowFile.exists(),
            listOf("main", "master").contains(branchName),
            refsByPrefix,
            hasUnreleasedChanges
        )
    }

    private fun openGitRepository(project: Project): Git? {
        try {
            return Git.open(project.rootProject.projectDir)
        } catch (e: Exception) {
            logger.warn("[axion-release] Could not open Git repository: ${e.message}")
            logger.warn("[axion-release] Using default version: ${project.version}")
        }
        return null
    }
}

data class RuntimeEnvironment(
    val branchName: String,
    val isShallow: Boolean,
    val isOnReleaseBranch: Boolean,
    val refsByPrefix: List<Ref>,
    val hasUnreleasedChanges: Boolean
)
