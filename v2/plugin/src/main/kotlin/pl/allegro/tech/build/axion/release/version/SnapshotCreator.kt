package pl.allegro.tech.build.axion.release.version

import pl.allegro.tech.build.axion.release.git.ScmPosition

fun interface SnapshotCreator {
    /**
     * Returns the complete string to append to the base version for a snapshot build
     * (including the leading separator, e.g. `"-feature-my-branch-SNAPSHOT"`),
     * or null when this should NOT be treated as a snapshot version.
     */
    fun create(version: String, position: ScmPosition): String?

    companion object {
        val SIMPLE: SnapshotCreator = simple()

        fun simple(suffix: String = "SNAPSHOT", separator: String = "-"): SnapshotCreator =
            SnapshotCreator { _, position -> "-${slugify(position.branch)}$separator$suffix" }
    }
}
