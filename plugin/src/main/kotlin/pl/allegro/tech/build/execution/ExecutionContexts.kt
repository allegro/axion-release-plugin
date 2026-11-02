package pl.allegro.tech.build.execution

import pl.allegro.tech.build.execution.ci.CiProvider
import pl.allegro.tech.build.execution.ci.github.GitHubProvider
import pl.allegro.tech.build.execution.ci.gitlab.GitLabProvider

internal object ExecutionContexts {
    private val providers: List<CiProvider> = listOf(GitHubProvider, GitLabProvider)

    val variables: Set<String> = setOf("PATH") + providers.flatMap { it.variables.values.flatten() }

    fun read(
        environment: Map<String, String>,
        readFile: (String) -> String,
    ): ExecutionContext {
        fun values(provider: CiProvider): EnvironmentVariables =
            EnvironmentVariables(provider.variables, environment::get)

        val local = ExecutionContext.Local(environment["PATH"]?.takeIf(String::isNotBlank))
        val selected = providers.firstOrNull { it.isActive(values(it)) }
        if (selected == null) return local
        return try {
            selected.read(values(selected), local, readFile)
        } catch (failure: ProviderUnavailableException) {
            local.copy(fallbackFailure = failure)
        }
    }
}
