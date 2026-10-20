package pl.allegro.tech.build.execution.context.ci

import pl.allegro.tech.build.execution.context.GitEnvironment
import pl.allegro.tech.build.execution.context.VersionTag
import pl.allegro.tech.build.execution.context.versionTag

internal sealed interface EventTrigger {
    val currentTag: String? get() = null

    fun tag(prefix: String, useHighestVersion: Boolean, gitEnvironment: GitEnvironment): VersionTag? {
        val tag = currentTag
        if (tag != null) return versionTag(tag, prefix)
        return if (useHighestVersion) gitEnvironment.highestVersionTag(prefix) else gitEnvironment.nearestVersionTag(prefix)
    }

    data class ReleasePublished(override val currentTag: String) : EventTrigger
    data class TagPush(override val currentTag: String) : EventTrigger
    data object BranchPush : EventTrigger
    data class Manual(val source: String) : EventTrigger
    data class Other(val name: String) : EventTrigger
}
