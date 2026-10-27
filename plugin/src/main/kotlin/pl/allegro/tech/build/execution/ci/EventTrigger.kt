package pl.allegro.tech.build.execution.ci

internal sealed interface EventTrigger {
    val currentTag: String? get() = null

    data class ReleasePublished(override val currentTag: String) : EventTrigger
    data class TagPush(override val currentTag: String) : EventTrigger
    data object BranchPush : EventTrigger
    data class Manual(val source: String) : EventTrigger
    data class Other(val name: String) : EventTrigger
}
