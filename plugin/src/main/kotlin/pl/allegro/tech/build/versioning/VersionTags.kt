package pl.allegro.tech.build.versioning

import com.github.zafarkhaja.semver.Version

internal fun versionTag(tag: String?, prefix: String): VersionTag? =
    tag?.let { parseVersionTag(it, prefix) }

internal fun versionTags(tags: Iterable<String>, prefix: String): List<VersionTag> =
    tags.mapNotNull { parseVersionTag(it, prefix) }

internal fun highestVersionTag(tags: Iterable<String>, prefix: String): VersionTag? =
    tags.asSequence()
        .mapNotNull { parseVersionTag(it, prefix) }
        .maxWithOrNull(Comparator<VersionTag> { left, right ->
            left.version.compareToIgnoreBuildMetadata(right.version)
        }.thenBy { it.name })

private fun parseVersionTag(tag: String, prefix: String): VersionTag? {
    if (!tag.startsWith(prefix)) return null
    val version = Version.tryParse(tag.removePrefix(prefix)).orElse(null) ?: return null
    return VersionTag(tag, version)
}
