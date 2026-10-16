package pl.allegro.tech.build.environment

import com.github.zafarkhaja.semver.Version
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

class GitHubRunnerEnvironment(environmentVariables: Map<String, String>) : GitEnvironment {

    private val event: GitHubEvent = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
        .adapter(GitHubEvent::class.java)
        .fromJson(File(requireNotNull(environmentVariables["GITHUB_EVENT_PATH"])).readText())
        ?: error("GitHub event must not be null")

    override val defaultBranchName: String = event.repository.default_branch
    override val currentBranchName: String = environmentVariables["GITHUB_REF_NAME"]!!
    override val headCommit: String = environmentVariables["GITHUB_SHA"]!!
    override val currentVersion: Version = Version.parse("1.0.0")

    data class GitHubEvent(var repository: Repository) {
        data class Repository(var default_branch: String)
    }
}
