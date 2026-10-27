package pl.allegro.tech.build.execution

internal class EnvironmentVariables(
    private val bindings: Map<EnvironmentField, List<String>>,
    private val lookup: (String) -> String?,
) {
    operator fun get(field: EnvironmentField): String = maybeGet(field)
        ?: throw ProviderUnavailableException(
            "Required environment variable ${
                bindings.getValue(field).joinToString(" or ")
            } is missing"
        )

    fun maybeGet(field: EnvironmentField): String? = bindings[field]
        ?.firstNotNullOfOrNull { name -> lookup(name)?.takeIf(String::isNotBlank) }
}
