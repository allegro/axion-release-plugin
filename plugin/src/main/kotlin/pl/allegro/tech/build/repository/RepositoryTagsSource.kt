package pl.allegro.tech.build.repository

import org.gradle.api.provider.ValueSource

internal abstract class RepositoryTagsSource : ValueSource<TagSelection, TagParameters> {
    override fun obtain(): TagSelection {
        val repository = Adapters.tagReader(
            parameters.backend.get(), parameters.executionContext(), parameters.directory.get().asFile, parameters.offline.get(),
        )
        return repository.resolveTags(parameters.prefix.get(), parameters.tagLookup.get())
    }
}
