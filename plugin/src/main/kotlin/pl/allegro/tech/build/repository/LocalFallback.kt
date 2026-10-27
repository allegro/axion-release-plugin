package pl.allegro.tech.build.repository

import org.gradle.api.logging.Logging
import pl.allegro.tech.build.execution.ProviderUnavailableException

internal object LocalFallback {
    private val logger = Logging.getLogger(LocalFallback::class.java)

    fun report(failure: ProviderUnavailableException) {
        logger.info("Tagline is using local Git: {}", failure.message)
    }

    fun <T> preservingFailure(failure: ProviderUnavailableException?, operation: () -> T): T = try {
        operation()
    } catch (localFailure: Exception) {
        if (failure != null && failure !== localFailure) localFailure.addSuppressed(failure)
        throw localFailure
    }
}
