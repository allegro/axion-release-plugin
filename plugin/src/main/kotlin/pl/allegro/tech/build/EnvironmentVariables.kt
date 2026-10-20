package pl.allegro.tech.build

internal class EnvironmentVariables(private val lookup: (String) -> String?) {

    operator fun get(name: String): String =
        requireNotNull(maybeGet(name)) { "Required environment variable $name is missing" }

    fun maybeGet(name: String): String? = lookup(name)

    operator fun contains(name: String): Boolean = maybeGet(name) != null
}
