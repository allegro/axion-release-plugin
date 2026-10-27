package pl.allegro.tech.build.execution

import pl.allegro.tech.build.execution.ci.EventTrigger
import pl.allegro.tech.build.extension.TagLookup
import pl.allegro.tech.build.repository.RepositoryMetadata
import pl.allegro.tech.build.repository.TagSelection

internal sealed interface ExecutionContext {
    data class Local(
        val executablePath: String?,
        val fallbackFailure: ProviderUnavailableException? = null,
    ) : ExecutionContext

    interface Ci : ExecutionContext {
        val metadata: RepositoryMetadata
        val event: EventTrigger
        val fallback: Local

        fun resolveTags(prefix: String, tagLookup: TagLookup, offline: Boolean): TagSelection
    }
}
