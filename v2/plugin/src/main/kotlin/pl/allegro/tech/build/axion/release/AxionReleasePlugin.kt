package pl.allegro.tech.build.axion.release

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import pl.allegro.tech.build.axion.release.git.LocalGitInfoProvider
import pl.allegro.tech.build.axion.release.git.ScmPosition
import pl.allegro.tech.build.axion.release.git.ci.CiDetector
import pl.allegro.tech.build.axion.release.git.ci.EnvironmentSource
import pl.allegro.tech.build.axion.release.version.VersionCalculator
import pl.allegro.tech.build.axion.release.version.VersionCalculatorConfig
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions

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

        registerTasks(project, extension)
    }

    private fun registerTasks(project: Project, extension: ScmVersionExtension) {
        val isDryRun: () -> Boolean = {
            project.findProperty("release.dryRun")?.toString()?.toBoolean() ?: false
        }

        project.tasks.register("currentVersion") { task ->
            task.group = "Release"
            task.description = "Prints the current project version derived from SCM tags."
            task.doLast {
                val quiet = project.findProperty("release.quiet")?.toString()?.toBoolean() ?: false
                if (quiet) println(project.version) else println("Project version: ${project.version}")
            }
        }

        project.tasks.register("verifyRelease") { task ->
            task.group = "Release"
            task.description = "Verifies the project is ready to release (no uncommitted changes, not a SNAPSHOT)."
            task.doLast {
                val version = project.version.toString()
                check(!version.contains("SNAPSHOT")) {
                    "[axion-release] Cannot release a SNAPSHOT version: $version"
                }
                logger.lifecycle("[axion-release] Version $version is ready for release.")
            }
        }

        // Named lifecycle hook tasks — add dependsOn/mustRunAfter to these to inject custom steps.
        // preRelease  → createRelease → postRelease → pushRelease → postPushRelease
        project.tasks.register("preRelease") { task ->
            task.group = "Release"
            task.description = "Lifecycle hook: runs before createRelease. Add dependsOn to inject pre-release steps."
            task.dependsOn("verifyRelease")
            task.doLast {
                val version = project.version.toString()
                val hookContext = HookContext(version, extension.previousVersion, project)
                extension.hooks.preHooks.forEach { it(hookContext) }
            }
        }

        project.tasks.register("createRelease") { task ->
            task.group = "Release"
            task.description = "Creates a release tag in the local repository."
            task.dependsOn("preRelease")
            task.doLast {
                val version = project.version.toString()
                val tagPrefix = extension.tag.resolvedPrefix(project)
                val tagName = "$tagPrefix$version"
                if (isDryRun()) {
                    logger.lifecycle("[axion-release] DRY RUN — would create tag $tagName")
                } else {
                    logger.lifecycle("[axion-release] Creating tag $tagName")
                    git("tag", tagName, workingDir = project.rootProject.projectDir, config = extension.repository)
                    logger.lifecycle("[axion-release] Tag $tagName created.")
                }
            }
        }

        project.tasks.register("postRelease") { task ->
            task.group = "Release"
            task.description = "Lifecycle hook: runs after createRelease but before pushRelease. Add dependsOn to inject post-create steps."
            task.mustRunAfter("createRelease")
            task.doLast {
                val version = project.version.toString()
                val hookContext = HookContext(version, extension.previousVersion, project)
                extension.hooks.postHooks.forEach { it(hookContext) }
            }
        }

        project.tasks.register("pushRelease") { task ->
            task.group = "Release"
            task.description = "Pushes the release tag to the remote repository."
            task.dependsOn("postRelease")
            task.mustRunAfter("createRelease")
            task.doLast {
                val version = project.version.toString()
                val tagPrefix = extension.tag.resolvedPrefix(project)
                val tagName = "$tagPrefix$version"
                if (isDryRun()) {
                    logger.lifecycle("[axion-release] DRY RUN — would push tag $tagName to ${extension.repository.remote}")
                } else {
                    logger.lifecycle("[axion-release] Pushing tag $tagName to ${extension.repository.remote}")
                    git("push", extension.repository.remote, tagName,
                        workingDir = project.rootProject.projectDir,
                        config = extension.repository)
                }
            }
        }

        project.tasks.register("postPushRelease") { task ->
            task.group = "Release"
            task.description = "Lifecycle hook: runs after pushRelease. Add dependsOn to inject post-push steps (e.g. deploy, notify)."
            task.mustRunAfter("pushRelease")
            task.doLast {
                val version = project.version.toString()
                val env = EnvironmentSource.SYSTEM
                CiDetector.detect(env)?.notifyRelease(version, env)
                logger.lifecycle("[axion-release] Released version $version")
            }
        }

        project.tasks.register("release") { task ->
            task.group = "Release"
            task.description = "Creates and pushes a release tag. Runs the full lifecycle: preRelease → createRelease → postRelease → pushRelease → postPushRelease."
            task.dependsOn("createRelease", "pushRelease", "postPushRelease")
        }
    }
}

private fun git(vararg args: String, workingDir: File, config: RepositoryConfig) {
    var tempKeyFile: File? = null
    try {
        val extraEnv = mutableMapOf<String, String>()
        when {
            config.customKey != null -> {
                val perms = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))
                tempKeyFile = Files.createTempFile("axion-key-", ".pem", perms).toFile()
                tempKeyFile.writeText(config.customKey!!)
                extraEnv["GIT_SSH_COMMAND"] = sshCommand(tempKeyFile.absolutePath, config.customKeyPassword)
            }
            config.customKeyFile != null -> {
                extraEnv["GIT_SSH_COMMAND"] = sshCommand(config.customKeyFile!!.absolutePath, config.customKeyPassword)
            }
        }

        val exitCode = ProcessBuilder("git", *args)
            .directory(workingDir)
            .inheritIO()
            .also { pb -> pb.environment().putAll(extraEnv) }
            .start()
            .waitFor()
        check(exitCode == 0) { "git ${args.joinToString(" ")} failed with exit code $exitCode" }
    } finally {
        tempKeyFile?.delete()
    }
}

private fun sshCommand(keyPath: String, password: String?): String {
    val base = "ssh -i $keyPath -o StrictHostKeyChecking=accept-new -o BatchMode=yes"
    return if (password != null) "$base -o IdentityAgent=none" else base
}
