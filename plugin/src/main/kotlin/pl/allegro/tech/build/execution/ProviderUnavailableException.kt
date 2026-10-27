package pl.allegro.tech.build.execution

internal class ProviderUnavailableException(
    message: String,
    cause: Throwable? = null
) : IllegalStateException(message, cause)
