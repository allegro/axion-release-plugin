package pl.allegro.tech.build.axion.release.git.ci

import java.io.File

class GithubActionsProvider : CiProvider {

    override fun isApplicable(env: EnvironmentSource): Boolean =
        env.get("GITHUB_ACTIONS") == "true"

    override fun getBranch(env: EnvironmentSource): String =
        env.get("GITHUB_REF_NAME") ?: env.get("GITHUB_REF")?.substringAfterLast("/") ?: "unknown"

    override fun getCurrentTag(env: EnvironmentSource, tagPrefix: String): String? {
        if (env.get("GITHUB_REF_TYPE") != "tag") return null
        val tagName = env.get("GITHUB_REF_NAME") ?: return null
        return tagName.takeIf { it.startsWith(tagPrefix) }
    }

    override fun notifyRelease(version: String, env: EnvironmentSource) {
        val outputFile = env.get("GITHUB_OUTPUT") ?: return
        File(outputFile).appendText("released-version=$version\n")
    }
}
