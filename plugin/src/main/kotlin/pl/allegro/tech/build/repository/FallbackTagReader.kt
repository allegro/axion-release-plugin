package pl.allegro.tech.build.repository

import pl.allegro.tech.build.execution.ProviderUnavailableException
import pl.allegro.tech.build.extension.TagLookup

internal class FallbackTagReader(
    private val preferred: TagReader,
    fallback: () -> TagReader,
    private val reportFallback: (ProviderUnavailableException) -> Unit = LocalFallback::report,
) : TagReader {
    private val local: TagReader by lazy(fallback)

    override fun resolveTags(prefix: String, tagLookup: TagLookup): TagSelection = try {
        preferred.resolveTags(prefix, tagLookup)
    } catch (failure: ProviderUnavailableException) {
        reportFallback(failure)
        LocalFallback.preservingFailure(failure) { local.resolveTags(prefix, tagLookup) }
    }
}
