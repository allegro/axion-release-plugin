package pl.allegro.tech.build.axion.release

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import pl.allegro.tech.build.axion.release.git.LocalGitInfoProvider
import pl.allegro.tech.build.axion.release.git.ScmPosition
import pl.allegro.tech.build.axion.release.git.ci.CiDetector
import pl.allegro.tech.build.axion.release.git.ci.EnvironmentSource
import pl.allegro.tech.build.axion.release.tasks.*
import pl.allegro.tech.build.axion.release.version.VersionCalculator
import pl.allegro.tech.build.axion.release.version.VersionCalculatorConfig

class AxionReleasePlugin : Plugin<Project> {

    private val logger = Logging.getLogger(AxionReleasePlugin::class.java)
    private val gitProvider = LocalGitInfoProvider()

    override fun apply(project: Project) {
        val extension = project.extensions.create("scmVersion", ScmVersionExtension::class.java, project)

        project.afterEvaluate {
            val env = EnvironmentSource.SYSTEM
            val tagPrefix = extension.tag.resolvedPrefix(project)
            val ciProvider = CiDetector.detect(env)

            val branchOverride = ciProvider?.getBranch(env)
            val tagOverride = ciProvider?.getCurrentTag(env, tagPrefix)

            if (ciProvider != null) {
                logger.info("[axion-release] CI detected: ${ciProvider::class.simpleName}")
            }

            val remote = if (extension.repository.unshallowRepoOnCI) extension.repository.remote else null
            val scmInfo = gitProvider.getInfo(
                project.rootProject.projectDir, tagPrefix, branchOverride, tagOverride,
                remote, extension.repository.useHighestVersion
            )

            if (scmInfo == null) {
                logger.warn("[axion-release] Could not determine SCM info, version remains 'unspecified'")
                return@afterEvaluate
            }

            extension.position = ScmPosition(
                branch = scmInfo.branch,
                revision = scmInfo.revision,
                shortRevision = scmInfo.shortRevision,
                isClean = !scmInfo.isDirty
            )
            extension.previousVersion = scmInfo.latestTag?.removePrefix(tagPrefix)

            val config = VersionCalculatorConfig(
                releaseBranches = extension.releaseBranches,
                initialVersion = extension.tag.initialVersion,
                versionIncrementer = extension.versionIncrementer,
                branchVersionIncrementer = extension.branchVersionIncrementer,
                snapshotCreator = extension.effectiveSnapshotCreator(),
                versionCreator = extension.versionCreator
            )

            // -Prelease.version=x.y.z forces a specific version (useful for hotfixes)
            val version = project.findProperty("release.version")?.toString()
                ?: VersionCalculator.calculate(scmInfo, tagPrefix, config)

            extension.version = version
            project.version = version

            logger.lifecycle("[axion-release] $tagPrefix → $version")
        }

        registerTasks(project)
    }

    private fun registerTasks(project: Project) {
        project.tasks.register("currentVersion", CurrentVersionTask::class.java)

        project.tasks.register("verifyRelease", VerifyReleaseTask::class.java)

        project.tasks.register("preRelease", PreReleaseTask::class.java) { task ->
            task.dependsOn("verifyRelease")
        }

        project.tasks.register("createRelease", CreateReleaseTask::class.java) { task ->
            task.dependsOn("preRelease")
        }

        project.tasks.register("postRelease", PostReleaseTask::class.java) { task ->
            task.mustRunAfter("createRelease")
        }

        project.tasks.register("pushRelease", PushReleaseTask::class.java) { task ->
            task.dependsOn("postRelease")
            task.mustRunAfter("createRelease")
        }

        project.tasks.register("postPushRelease", PostPushReleaseTask::class.java) { task ->
            task.mustRunAfter("pushRelease")
        }

        project.tasks.register("release") { task ->
            task.group = "Release"
            task.description = "Creates and pushes a release tag. Runs the full lifecycle: preRelease → createRelease → postRelease → pushRelease → postPushRelease."
            task.dependsOn("createRelease", "pushRelease", "postPushRelease")
        }
    }
}
