package pl.allegro.tech.build.execution.context.local

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import pl.allegro.tech.build.execution.context.VersionTag
import pl.allegro.tech.build.execution.context.highestVersionTag
import pl.allegro.tech.build.execution.context.versionTags
import pl.allegro.tech.build.extension.GitSpec
import java.io.File

internal class JGitEnvironment(
    internal val options: GitSpec,
    private val directory: File,
) : LocalRepository {
    private val git: Git by lazy {
        Git.wrap(FileRepositoryBuilder().findGitDir(directory).build())
    }

    override val defaultBranchName: String
        get() = git.repository.findRef("refs/remotes/origin/HEAD")
            ?.target
            ?.name
            ?.substringAfterLast('/')
            ?: error("Default branch not found")

    override val currentBranchName: String
        get() = git.repository.branch

    override val headCommit: String
        get() = git.repository.resolve("HEAD")?.name ?: error("HEAD commit not found")

    override fun highestVersionTag(prefix: String): VersionTag? {
        val tags = git.tagList().call().map { it.name.removePrefix(Constants.R_TAGS) }
        return highestVersionTag(tags, prefix)
    }

    override fun nearestVersionTag(prefix: String): VersionTag? {
        val tags = git.tagList().call().map { it.name.removePrefix(Constants.R_TAGS) }
        val matchingTags = versionTags(tags, prefix)
        if (matchingTags.isEmpty()) return null

        val name = git.describe()
            .setTags(true)
            .setAbbrev(0)
            .setMatch(*matchingTags.map { it.name }.toTypedArray())
            .call()
        return matchingTags.firstOrNull { it.name == name }
    }
}
