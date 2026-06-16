package pl.allegro.tech.build.axion.release.version

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.git.ScmInfo
import pl.allegro.tech.build.axion.release.git.ScmPosition

object VersionCalculator {

    fun calculate(info: ScmInfo, tagPrefix: String, config: VersionCalculatorConfig): String {
        val isReleaseBranch = config.releaseBranches.contains(info.branch)
        val position = ScmPosition(
            branch = info.branch,
            revision = info.revision,
            shortRevision = info.shortRevision,
            isClean = !info.isDirty
        )

        if (info.currentTag != null) {
            return config.versionCreator.create(info.currentTag.removePrefix(tagPrefix), info)
        }

        val baseVersion = if (info.latestTag == null) {
            config.initialVersion
        } else {
            val base = Version.parse(info.latestTag.removePrefix(tagPrefix))
            resolveIncrementer(info, config).increment(base, info).toString()
        }

        return if (isReleaseBranch) {
            config.versionCreator.create(baseVersion, info)
        } else {
            val snapshotSuffix = config.snapshotCreator.create(baseVersion, position)
            val withSnapshot = if (snapshotSuffix != null) "$baseVersion$snapshotSuffix" else baseVersion
            config.versionCreator.create(withSnapshot, info)
        }
    }

    private fun resolveIncrementer(info: ScmInfo, config: VersionCalculatorConfig): VersionIncrementer =
        config.branchVersionIncrementer.entries
            .firstOrNull { (pattern, _) -> Regex(pattern).matches(info.branch) }
            ?.value ?: config.versionIncrementer
}
