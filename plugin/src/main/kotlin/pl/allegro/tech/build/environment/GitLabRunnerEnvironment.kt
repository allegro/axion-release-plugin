package pl.allegro.tech.build.environment

import com.github.zafarkhaja.semver.Version

class GitLabRunnerEnvironment(environmentVariables: Map<String, String>) : GitEnvironment {
    override val defaultBranchName: String = environmentVariables["CI_DEFAULT_BRANCH"]!!
    override val currentBranchName: String = environmentVariables["CI_COMMIT_REF_NAME"]!!
    override val headCommit: String = environmentVariables["CI_COMMIT_SHA"]!!
    override val currentVersion: Version = Version.parse("1.0.0")
}
