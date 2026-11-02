package pl.allegro.tech.build.repository

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import pl.allegro.tech.build.execution.ExecutionContexts
import pl.allegro.tech.build.extension.Backend
import pl.allegro.tech.build.extension.TagLookup
import pl.allegro.tech.build.repository.RepositoryTagsSource.Parameters

internal abstract class RepositoryTagsSource : ValueSource<TagSelection, Parameters> {

    override fun obtain(): TagSelection {
        val executionContext = ExecutionContexts.resolve(parameters.environment.get())
        val repository = Adapters.tagReader(
            parameters.backend.get(),
            executionContext,
            parameters.directory.get().asFile,
            parameters.offline.get(),
        )
        return repository.resolveTags(parameters.prefix.get(), parameters.tagLookup.get())
    }

    internal interface Parameters : ValueSourceParameters {
        val backend: Property<Backend>
        val environment: MapProperty<String, String>
        val directory: DirectoryProperty
        val prefix: Property<String>
        val tagLookup: Property<TagLookup>
        val offline: Property<Boolean>
    }
}