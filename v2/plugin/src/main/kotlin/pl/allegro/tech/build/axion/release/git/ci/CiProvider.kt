package pl.allegro.tech.build.axion.release.git.ci

interface CiProvider {
    fun isApplicable(env: EnvironmentSource): Boolean

    // Returns the branch name as reported by the CI system.
    fun getBranch(env: EnvironmentSource): String

    // Returns the tag name (with prefix) when HEAD is a tagged build, null otherwise.
    fun getCurrentTag(env: EnvironmentSource, tagPrefix: String): String?

    // Notifies the CI system that a release was performed. Default is a no-op.
    fun notifyRelease(version: String, env: EnvironmentSource) {}
}
