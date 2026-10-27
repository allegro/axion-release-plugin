package pl.allegro.tech.build.repository

import org.gradle.api.provider.ValueSource

internal abstract class RepositoryMetadataSource : ValueSource<RepositoryMetadata, RepositoryParameters> {
    override fun obtain(): RepositoryMetadata = Adapters.metadataReader(
        parameters.backend.get(), parameters.executionContext(), parameters.directory.get().asFile,
    ).readMetadata()
}
