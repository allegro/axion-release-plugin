package pl.allegro.tech.build.execution.ci

import pl.allegro.tech.build.execution.EnvironmentField
import pl.allegro.tech.build.execution.EnvironmentVariables
import pl.allegro.tech.build.execution.ExecutionContext

internal interface ExecutionContextResolver {
    val variables: Map<EnvironmentField, List<String>>

    fun isActive(environment: EnvironmentVariables): Boolean =
        environment.maybeGet(EnvironmentField.CI_HINT) == "true"

    fun resolve(
        environment: EnvironmentVariables,
        fallback: ExecutionContext.Local,
    ): ExecutionContext.Ci
}
