package pl.allegro.tech.build.execution.ci.gitlab

import pl.allegro.tech.build.execution.ci.EventTrigger

internal object GitLabEvents {
    fun detect(commitTag: String?, source: String?): EventTrigger =
        commitTag?.takeIf(String::isNotBlank)?.let(EventTrigger::TagPush)
            ?: when (source) {
                "push" -> EventTrigger.BranchPush
                "web", "api", "trigger" -> EventTrigger.Manual(source)
                else -> EventTrigger.Other(source ?: "unknown")
            }
}
