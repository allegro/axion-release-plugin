package pl.allegro.tech.build.repository

import org.gradle.api.provider.Property
import pl.allegro.tech.build.extension.TagLookup

internal interface TagParameters : RepositoryParameters {
    val prefix: Property<String>
    val tagLookup: Property<TagLookup>
    val offline: Property<Boolean>
}
