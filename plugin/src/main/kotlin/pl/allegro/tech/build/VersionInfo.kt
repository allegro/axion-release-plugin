package pl.allegro.tech.build

import com.github.zafarkhaja.semver.Version

data class VersionInfo(
    val currentVersion: String,
    val nextVersion: Version,
    val baseVersion: Version,
    val releaseTag: String?,
    val revision: String,
    val branch: String?,
    val snapshot: Boolean,
)
