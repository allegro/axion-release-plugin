package pl.allegro.tech.build.execution.ci.github

import pl.allegro.tech.build.execution.ProviderUnavailableException
import pl.allegro.tech.build.extension.TagLookup
import pl.allegro.tech.build.repository.TagReader
import pl.allegro.tech.build.repository.TagSelection
import pl.allegro.tech.build.versioning.highestVersionTag
import pl.allegro.tech.build.versioning.versionTag

internal class GitHubTagReader(private val context: GitHubProvider.Context, private val offline: Boolean) : TagReader {
    override fun resolveTags(prefix: String, tagLookup: TagLookup): TagSelection {
        val eventTag = context.event.currentTag
        if (eventTag != null) {
            requireNotNull(versionTag(eventTag, prefix)) {
                "GitHub event tag '$eventTag' must match prefix '$prefix' and contain a SemVer version"
            }
            return TagSelection(eventTag, eventTag)
        }
        if (tagLookup != TagLookup.HIGHEST_GLOBAL) {
            throw ProviderUnavailableException("GitHub $tagLookup lookup is not supported yet")
        }
        if (offline) throw ProviderUnavailableException("Offline mode prevents CI tag API lookup")
        val tags = GitHubApiClient(context.apiAccess()).use { it.tags() }
        val exact = highestVersionTag(tags.filter { it.commit == context.metadata.revision }.map { it.name }, prefix)
        val base = exact ?: highestVersionTag(tags.map { it.name }, prefix)
        return TagSelection(exact?.name, base?.name)
    }
}
