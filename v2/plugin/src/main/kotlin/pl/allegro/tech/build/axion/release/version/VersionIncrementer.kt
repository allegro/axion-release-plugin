package pl.allegro.tech.build.axion.release.version

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.git.ScmInfo

fun interface VersionIncrementer {
    fun increment(version: Version, info: ScmInfo): Version

    companion object {
        val PATCH: VersionIncrementer = VersionIncrementer { v, _ ->
            v.nextPatchVersion()
        }
        val MINOR: VersionIncrementer = VersionIncrementer { v, _ ->
            v.nextMinorVersion()
        }
        val MAJOR: VersionIncrementer = VersionIncrementer { v, _ ->
            v.nextMajorVersion()
        }

        /**
         * Increments MINOR when on a non-release branch (new feature), PATCH when on a release branch (hotfix).
         * Matches branch names against [releaseBranches] using full regex matching.
         */
        fun incrementMinorIfNotOnRelease(releaseBranches: List<String>): VersionIncrementer =
            VersionIncrementer { v, info ->
                val onRelease = releaseBranches.any { pattern -> Regex(pattern).matches(info.branch) }
                if (onRelease) {
                    v.nextPatchVersion()
                } else {
                    v.nextMinorVersion()
                }
            }
    }
}
