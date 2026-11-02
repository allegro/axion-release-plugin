package pl.allegro.tech.build.execution

import pl.allegro.tech.build.execution.ci.ExecutionContextResolver
import pl.allegro.tech.build.execution.ci.github.GitHubContext
import pl.allegro.tech.build.execution.ci.gitlab.GitLabContext

internal object ExecutionContexts {
    private val providers: List<ExecutionContextResolver> = listOf(GitHubContext, GitLabContext)

    val variables: Set<String> = setOf("PATH") + providers.flatMap { it.variables.values.flatten() }

    fun resolve(
        environment: Map<String, String>,
    ): ExecutionContext {
        fun values(provider: ExecutionContextResolver): EnvironmentVariables =
            EnvironmentVariables(provider.variables, environment::get)

        val local = ExecutionContext.Local(environment["PATH"]?.takeIf(String::isNotBlank))
        val selected = providers.firstOrNull { it.isActive(values(it)) }
        if (selected == null) return local
        return try {
            selected.resolve(values(selected), local)
        } catch (failure: ProviderUnavailableException) {
            local.copy(fallbackFailure = failure)
        }
    }
}
