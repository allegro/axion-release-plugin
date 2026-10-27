package pl.allegro.tech.build.versioning

import com.github.zafarkhaja.semver.Version

internal data class VersionTag(
    val name: String,
    val version: Version
)
