package pl.allegro.tech.build.environment

import com.github.bgalek.github.dotcom.models.WebhookReleasePublished
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

class GitHubRunnerEnvironment(environmentVariables: Map<String, String>) : GitEnvironment {

    private val event: WebhookReleasePublished = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
        .adapter(WebhookReleasePublished::class.java)
        .fromJson(File(requireNotNull(environmentVariables["GITHUB_EVENT_PATH"])).readText())
        ?: error("GitHub event must not be null")

    override val defaultBranchName: String = event.repository.defaultBranch
    override val currentBranchName: String = environmentVariables["GITHUB_REF_NAME"]!!
    override val headCommit: String = environmentVariables["GITHUB_SHA"]!!
    override fun getTag(prefix: String): String {
        val tag = event.release.tagName
        if (!tag.startsWith(prefix)) {
            throw IllegalArgumentException("Tag $tag does not start with prefix $prefix")
        }
        return tag;
    }
}
