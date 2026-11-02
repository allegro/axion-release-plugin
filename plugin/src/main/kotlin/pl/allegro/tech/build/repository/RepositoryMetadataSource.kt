package pl.allegro.tech.build.repository

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import pl.allegro.tech.build.execution.ExecutionContexts
import pl.allegro.tech.build.extension.Backend
import pl.allegro.tech.build.repository.RepositoryMetadataSource.Parameters

internal abstract class RepositoryMetadataSource : ValueSource<RepositoryMetadata, Parameters> {
    override fun obtain(): RepositoryMetadata {
        val executionContext = ExecutionContexts.resolve(parameters.environment.get())
        return Adapters.metadataReader(
            parameters.backend.get(),
            executionContext,
            parameters.directory.get().asFile,
        ).readMetadata()
    }

    internal interface Parameters : ValueSourceParameters {
        val backend: Property<Backend>
        val environment: MapProperty<String, String>
        val directory: DirectoryProperty
    }
}
