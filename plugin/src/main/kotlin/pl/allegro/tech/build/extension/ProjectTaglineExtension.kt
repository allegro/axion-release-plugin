package pl.allegro.tech.build.extension

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.VersionInfo

/** Resolved during settings evaluation, before any project or publication is configured. */
class ProjectTaglineExtension(val versionInfo: VersionInfo) {
    val currentVersion: String get() = versionInfo.currentVersion
    val nextVersion: Version get() = versionInfo.nextVersion
}
