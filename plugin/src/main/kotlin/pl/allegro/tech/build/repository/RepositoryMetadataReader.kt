package pl.allegro.tech.build.repository

internal fun interface RepositoryMetadataReader {
    fun readMetadata(): RepositoryMetadata
}
