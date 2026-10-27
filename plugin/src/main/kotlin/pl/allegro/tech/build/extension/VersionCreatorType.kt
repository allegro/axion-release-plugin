package pl.allegro.tech.build.extension

enum class VersionCreatorType(private val creator: VersionCreator) : VersionCreator {
    VERSION({ version, _ -> version }),
    VERSION_WITH_BRANCH({ version, position ->
        position.branch?.let { "$version-$it" } ?: version
    }),
    VERSION_WITH_COMMIT_SHA({ version, position -> "$version-${position.shortRevision}" });

    override fun create(version: String, position: Position): String = creator.create(version, position)
}
