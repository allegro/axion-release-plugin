package pl.allegro.tech.build.execution.context.ci.github

import com.github.bgalek.github.dotcom.models.RepositoryWebhooks
import com.github.bgalek.github.dotcom.models.WebhookPush
import com.github.bgalek.github.dotcom.models.WebhookReleaseCreated
import com.github.bgalek.github.dotcom.models.WebhookReleaseDeleted
import com.github.bgalek.github.dotcom.models.WebhookReleaseEdited
import com.github.bgalek.github.dotcom.models.WebhookReleasePrereleased
import com.github.bgalek.github.dotcom.models.WebhookReleasePublished
import com.github.bgalek.github.dotcom.models.WebhookReleaseReleased
import com.github.bgalek.github.dotcom.models.WebhookReleaseUnpublished
import com.github.bgalek.github.dotcom.models.WebhookWorkflowDispatch
import pl.allegro.tech.build.execution.context.ci.EventTrigger
import tools.jackson.core.JsonToken
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.json.JsonMapper
import java.io.File

internal class GitHubEvents(name: String?, refType: String?, refName: String?, path: String?) {
    private val file = path?.let(::File)
    private val releaseAction = if (name == "release") file?.let(::readAction) else null
    private val publishedRelease = if (releaseAction == "published") {
        file?.let { mapper.readValue(it, WebhookReleasePublished::class.java) }
    } else null

    val event: EventTrigger = when (name) {
        "release" -> if (releaseAction == "published") {
            publishedRelease?.release?.tagName?.takeIf(String::isNotBlank)
                ?.let(EventTrigger::ReleasePublished)
                ?: EventTrigger.Other("release:published")
        } else {
            EventTrigger.Other("release:${releaseAction ?: "unknown"}")
        }
        "push" -> if (refType == "tag") {
            refName?.takeIf(String::isNotBlank)?.let(EventTrigger::TagPush)
                ?: EventTrigger.Other("push:tag")
        } else {
            EventTrigger.BranchPush
        }
        "workflow_dispatch" -> EventTrigger.Manual(name)
        else -> EventTrigger.Other(name ?: "unknown")
    }

    val defaultBranch: String? = file?.let { payload ->
        when (name) {
            "release" -> releaseDefaultBranch(payload)
            "push" -> mapper.readValue(payload, WebhookPush::class.java).repository?.defaultBranch
            "workflow_dispatch" -> mapper.readValue(payload, WebhookWorkflowDispatch::class.java).repository?.defaultBranch
            else -> repositoryDefaultBranch(payload)
        }
    }

    private fun releaseDefaultBranch(file: File): String? = when (releaseAction) {
        "published" -> publishedRelease?.repository?.defaultBranch
        "created" -> mapper.readValue(file, WebhookReleaseCreated::class.java).repository?.defaultBranch
        "deleted" -> mapper.readValue(file, WebhookReleaseDeleted::class.java).repository?.defaultBranch
        "edited" -> mapper.readValue(file, WebhookReleaseEdited::class.java).repository?.defaultBranch
        "prereleased" -> mapper.readValue(file, WebhookReleasePrereleased::class.java).repository?.defaultBranch
        "released" -> mapper.readValue(file, WebhookReleaseReleased::class.java).repository?.defaultBranch
        "unpublished" -> mapper.readValue(file, WebhookReleaseUnpublished::class.java).repository?.defaultBranch
        else -> repositoryDefaultBranch(file)
    }

    private fun readAction(file: File): String? = mapper.createParser(file).use { parser ->
        parser.nextToken()
        while (parser.nextToken() == JsonToken.PROPERTY_NAME) {
            val property = parser.currentName()
            parser.nextToken()
            if (property == "action") {
                return@use if (parser.currentToken() == JsonToken.VALUE_STRING) parser.getString() else null
            }
            parser.skipChildren()
        }
        null
    }

    private fun repositoryDefaultBranch(file: File): String? = mapper.createParser(file).use { parser ->
        parser.nextToken()
        while (parser.nextToken() == JsonToken.PROPERTY_NAME) {
            val property = parser.currentName()
            parser.nextToken()
            if (property == "repository") {
                return@use mapper.readerFor(RepositoryWebhooks::class.java)
                    .without(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                    .readValue<RepositoryWebhooks>(parser)
                    ?.defaultBranch
            }
            parser.skipChildren()
        }
        null
    }

    companion object {
        private val mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build()
    }
}
