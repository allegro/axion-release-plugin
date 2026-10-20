package pl.allegro.tech.build

import org.gradle.api.GradleException
import pl.allegro.tech.build.execution.context.GitEnvironment
import pl.allegro.tech.build.execution.context.ci.github.GitHubRunnerEnvironment
import pl.allegro.tech.build.execution.context.ci.gitlab.GitLabRunnerEnvironment
import pl.allegro.tech.build.execution.context.local.JGitEnvironment
import pl.allegro.tech.build.execution.context.local.LocalGitEnvironment
import pl.allegro.tech.build.execution.context.local.LocalRepository
import pl.allegro.tech.build.extension.Backend
import pl.allegro.tech.build.extension.TaglineExtension
import java.io.File

internal object Adapters {
    private enum class Host { GITHUB, GITLAB, LOCAL }

    fun getAdapter(
        extension: TaglineExtension,
        environment: EnvironmentVariables,
        directory: File,
    ): GitEnvironment {
        val backend = extension.backend.get()
        return pickGitEnvironment(
            detectHost(backend, environment),
            environment,
            extension,
            directory,
            backend
        )
    }

    private fun pickGitEnvironment(
        host: Host,
        environment: EnvironmentVariables,
        extension: TaglineExtension,
        directory: File,
        backend: Backend
    ): GitEnvironment = when (host) {
        Host.GITHUB -> GitHubRunnerEnvironment(
            environment, extension.github, localRepository(extension, environment.maybeGet("PATH"), directory)
        )

        Host.GITLAB -> GitLabRunnerEnvironment(
            environment, extension.gitlab, localRepository(extension, environment.maybeGet("PATH"), directory)
        )

        Host.LOCAL -> when (backend) {
            Backend.GIT -> {
                requireEnvironment(LocalGitEnvironment.isAvailable(environment.maybeGet("PATH")), backend)
                LocalGitEnvironment(extension.git, directory)
            }

            Backend.JGIT -> JGitEnvironment(extension.git, directory)
            else -> localRepository(extension, environment.maybeGet("PATH"), directory)
        }
    }

    private fun localRepository(
        extension: TaglineExtension,
        path: String?,
        directory: File,
    ): LocalRepository = if (LocalGitEnvironment.isAvailable(path)) {
        LocalGitEnvironment(extension.git, directory)
    } else {
        JGitEnvironment(extension.git, directory)
    }

    private fun detectHost(
        backend: Backend,
        environment: EnvironmentVariables
    ): Host {
        return when (backend) {
            Backend.AUTO -> detectHost(environment)
            Backend.GITHUB -> {
                requireEnvironment("GITHUB_ACTIONS" in environment, backend)
                Host.GITHUB
            }

            Backend.GITLAB -> {
                requireEnvironment("GITLAB_CI" in environment, backend)
                Host.GITLAB
            }

            Backend.GIT, Backend.JGIT -> Host.LOCAL
        }
    }

    private fun detectHost(environment: EnvironmentVariables): Host = when {
        "GITHUB_ACTIONS" in environment -> Host.GITHUB
        "GITLAB_CI" in environment -> Host.GITLAB
        else -> Host.LOCAL
    }

    private fun requireEnvironment(applicable: Boolean, backend: Backend) {
        if (!applicable) {
            throw GradleException("Configured tagline backend $backend is not applicable in this environment.")
        }
    }
}
