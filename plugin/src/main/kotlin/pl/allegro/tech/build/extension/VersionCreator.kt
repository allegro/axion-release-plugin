package pl.allegro.tech.build.extension

data class Position(
    val revision: String,
    val branch: String?,
    val onTag: Boolean,
    val dirty: Boolean,
) {
    val shortRevision: String get() = revision.take(7)
}

fun interface VersionCreator {
    fun create(version: String, position: Position): String
}

enum class VersionCreatorType(private val creator: VersionCreator) : VersionCreator {
    VERSION({ version, _ -> version }),
    VERSION_WITH_BRANCH({ version, position ->
        position.branch?.let { "$version-${it.replace(Regex("[^A-Za-z0-9._-]"), "-")}" } ?: version
    }),
    VERSION_WITH_COMMIT_SHA({ version, position -> "$version-${position.shortRevision}" });

    override fun create(version: String, position: Position): String = creator.create(version, position)
}

fun interface AnnotationMessage {
    fun create(position: Position, version: String, previousVersion: String?): String
}
