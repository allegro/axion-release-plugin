package pl.allegro.tech.build.axion.release.version

import pl.allegro.tech.build.axion.release.git.ScmInfo

fun interface VersionCreator {
    /** Post-processes the fully assembled version string (applied after snapshot suffix). */
    fun create(version: String, info: ScmInfo): String

    companion object {
        /** No-op — version passes through unchanged. */
        val SIMPLE: VersionCreator = VersionCreator { version, _ -> version }

        /** Appends the slugified branch name. Use instead of SIMPLE SnapshotCreator. */
        val WITH_BRANCH: VersionCreator = VersionCreator { version, info ->
            "$version-${slugify(info.branch)}"
        }

        /** Appends the short commit hash. Falls back to version unchanged when no hash available. */
        val WITH_COMMIT_HASH: VersionCreator = VersionCreator { version, info ->
            if (info.shortRevision.isNotEmpty()) "$version-${info.shortRevision}" else version
        }
    }
}
