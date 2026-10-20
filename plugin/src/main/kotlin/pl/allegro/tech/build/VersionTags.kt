package pl.allegro.tech.build.execution.context

import com.github.zafarkhaja.semver.Version

internal fun versionTag(tag: String?, prefix: String): VersionTag? =
    tag?.let { parseVersionTag(it, prefix) }

internal fun versionTags(tags: Iterable<String>, prefix: String): List<VersionTag> =
    tags.mapNotNull { parseVersionTag(it, prefix) }

internal fun highestVersionTag(tags: Iterable<String>, prefix: String): VersionTag? =
    versionTags(tags, prefix)
        .maxWithOrNull(compareBy<VersionTag> { it.version }.thenBy { it.name })

private fun parseVersionTag(tag: String, prefix: String): VersionTag? {
    if (!tag.startsWith(prefix)) return null
    val version = Version.tryParse(tag.removePrefix(prefix)).orElse(null) ?: return null
    return VersionTag(tag, version)
}

internal data class VersionTag(
    val name: String,
    val version: Version
)
