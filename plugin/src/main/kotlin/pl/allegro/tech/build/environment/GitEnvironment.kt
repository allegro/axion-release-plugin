package pl.allegro.tech.build.environment

interface GitEnvironment {
    val defaultBranchName: String
    val currentBranchName: String
    val headCommit: String
    fun getTag(prefix: String): String
}
