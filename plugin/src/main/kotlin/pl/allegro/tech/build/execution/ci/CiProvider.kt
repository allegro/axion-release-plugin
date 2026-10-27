package pl.allegro.tech.build.execution.ci

import pl.allegro.tech.build.execution.EnvironmentField
import pl.allegro.tech.build.execution.EnvironmentVariables
import pl.allegro.tech.build.execution.ExecutionContext

internal interface CiProvider {
    val variables: Map<EnvironmentField, List<String>>

    fun isActive(environment: EnvironmentVariables): Boolean =
        environment.maybeGet(EnvironmentField.CI_HINT) == "true"

    fun read(
        environment: EnvironmentVariables,
        fallback: ExecutionContext.Local,
        readFile: (String) -> String,
    ): ExecutionContext.Ci
}
