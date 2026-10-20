package pl.allegro.tech.build.execution.context

internal interface GitEnvironment {
    val defaultBranchName: String
    val currentBranchName: String
    val headCommit: String
    fun highestVersionTag(prefix: String): VersionTag?
    fun nearestVersionTag(prefix: String): VersionTag?
    fun getTag(prefix: String, useHighestVersion: Boolean = true): VersionTag? =
        if (useHighestVersion) highestVersionTag(prefix) else nearestVersionTag(prefix)
}
