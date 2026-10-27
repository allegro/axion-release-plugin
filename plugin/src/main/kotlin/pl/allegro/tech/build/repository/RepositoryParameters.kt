package pl.allegro.tech.build.repository

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSourceParameters
import pl.allegro.tech.build.execution.ExecutionContext
import pl.allegro.tech.build.execution.ExecutionContexts
import pl.allegro.tech.build.extension.Backend

/** External state is a configuration input, including when a configuration cache entry is reused. */
internal interface RepositoryParameters : ValueSourceParameters {
    val backend: Property<Backend>
    val environment: MapProperty<String, String>
    val directory: DirectoryProperty
}

internal fun RepositoryParameters.executionContext(): ExecutionContext =
    ExecutionContexts.read(environment.get()) { path -> directory.get().asFile.resolve(path).readText() }
