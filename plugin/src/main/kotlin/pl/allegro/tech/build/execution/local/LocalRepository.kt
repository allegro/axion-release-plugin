package pl.allegro.tech.build.execution.local

import pl.allegro.tech.build.repository.RepositoryMetadataReader
import pl.allegro.tech.build.repository.TagReader

internal interface LocalRepository : RepositoryMetadataReader, TagReader
