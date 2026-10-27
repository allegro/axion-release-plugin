package pl.allegro.tech.build.repository

internal data class RepositoryMetadata(
    val revision: String,
    val branch: String?,
    val defaultBranch: String?,
)
