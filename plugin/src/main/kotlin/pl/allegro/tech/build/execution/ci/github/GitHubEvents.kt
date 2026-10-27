package pl.allegro.tech.build.execution.ci.github

import pl.allegro.tech.build.execution.ProviderUnavailableException
import pl.allegro.tech.build.execution.ci.EventTrigger
import tools.jackson.core.JacksonException
import tools.jackson.databind.json.JsonMapper

internal class GitHubEvents(name: String?, refType: String?, refName: String?, payload: String?) {
    private val root = try {
        payload?.let { JsonMapper().readTree(it) }
    } catch (failure: JacksonException) {
        throw ProviderUnavailableException("GitHub event payload could not be parsed", failure)
    }

    val event: EventTrigger = when (name) {
        "release" -> {
            if (root?.path("action")?.asString() != "published") {
                throw ProviderUnavailableException("Only published GitHub release events are supported for version resolution")
            }
            EventTrigger.ReleasePublished(root.path("release").path("tag_name").asString()
                ?: throw ProviderUnavailableException("GitHub published release payload is missing release.tag_name"))
        }
        "push" -> if (refType == "tag") {
            EventTrigger.TagPush(refName ?: throw ProviderUnavailableException("GitHub tag push is missing GITHUB_REF_NAME"))
        } else EventTrigger.BranchPush
        "workflow_dispatch" -> EventTrigger.Manual(name)
        else -> throw ProviderUnavailableException("GitHub event '${name ?: "unknown"}' is not supported yet for version resolution")
    }

    val defaultBranch: String? = root?.path("repository")?.path("default_branch")
        ?.asString()?.takeIf(String::isNotBlank)
}
