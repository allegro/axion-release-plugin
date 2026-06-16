package pl.allegro.tech.build.axion.release.git.ci

import java.io.File

class GitlabCiProvider : CiProvider {

    override fun isApplicable(env: EnvironmentSource): Boolean =
        env.get("GITLAB_CI") == "true"

    override fun getBranch(env: EnvironmentSource): String =
        env.get("CI_COMMIT_BRANCH")
            ?: env.get("CI_COMMIT_REF_NAME")
            ?: "unknown"

    override fun getCurrentTag(env: EnvironmentSource, tagPrefix: String): String? {
        val tag = env.get("CI_COMMIT_TAG")?.takeIf { it.isNotEmpty() } ?: return null
        return tag.takeIf { it.startsWith(tagPrefix) }
    }

    override fun notifyRelease(version: String, env: EnvironmentSource) {
        val envFile = env.get("CI_ENV_FILE")
        if (envFile != null) {
            File(envFile).appendText("RELEASED_VERSION=$version\n")
        } else {
            println("RELEASED_VERSION=$version")
        }
    }
}
