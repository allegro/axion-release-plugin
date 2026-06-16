package pl.allegro.tech.build.axion.release.git.ci

import org.gradle.api.logging.Logging

class JenkinsProvider : CiProvider {

    private val logger = Logging.getLogger(JenkinsProvider::class.java)

    override fun isApplicable(env: EnvironmentSource): Boolean =
        env.get("JENKINS_URL") != null

    override fun getBranch(env: EnvironmentSource): String {
        val raw = env.get("GIT_BRANCH") ?: env.get("BRANCH_NAME") ?: return "unknown"
        // Strip "origin/" or any other remote prefix (first path segment)
        return if (raw.contains("/")) raw.substringAfter("/") else raw
    }

    override fun getCurrentTag(env: EnvironmentSource, tagPrefix: String): String? {
        val tag = env.get("GIT_TAG_NAME")?.takeIf { it.isNotEmpty() } ?: return null
        return tag.takeIf { it.startsWith(tagPrefix) }
    }

    // Jenkins has no standard output mechanism; log a parseable line for downstream use
    override fun notifyRelease(version: String, env: EnvironmentSource) {
        logger.lifecycle("[axion-release] released-version=$version")
    }
}
