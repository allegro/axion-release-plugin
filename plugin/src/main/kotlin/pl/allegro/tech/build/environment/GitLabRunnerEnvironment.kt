package pl.allegro.tech.build.environment

class GitLabRunnerEnvironment(environmentVariables: Map<String, String>) : GitEnvironment {
    override val defaultBranchName: String = environmentVariables["CI_DEFAULT_BRANCH"]!!
    override val currentBranchName: String = environmentVariables["CI_COMMIT_REF_NAME"]!!
    override val headCommit: String = environmentVariables["CI_COMMIT_SHA"]!!
    override fun getTag(prefix: String): String {
        TODO("getTag is not implemented for GitLabRunnerEnvironment")
    }
}
