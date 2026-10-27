package pl.allegro.tech.build.repository

import pl.allegro.tech.build.extension.TagLookup

internal fun interface TagReader {
    fun resolveTags(prefix: String, tagLookup: TagLookup): TagSelection
}
