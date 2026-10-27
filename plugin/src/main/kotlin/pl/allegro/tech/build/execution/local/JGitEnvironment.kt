package pl.allegro.tech.build.execution.local

import java.io.File
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import pl.allegro.tech.build.extension.TagLookup
import pl.allegro.tech.build.repository.RepositoryMetadata
import pl.allegro.tech.build.repository.TagSelection
import pl.allegro.tech.build.versioning.VersionTag
import pl.allegro.tech.build.versioning.highestVersionTag
import pl.allegro.tech.build.versioning.versionTags

internal class JGitEnvironment(
    private val directory: File,
) : LocalRepository {
    private fun <T> withGit(operation: (Git) -> T): T =
        FileRepositoryBuilder().findGitDir(directory).build().use { repository ->
            operation(Git.wrap(repository))
        }

    override fun readMetadata(): RepositoryMetadata = withGit { git ->
        val head = git.repository.resolve("HEAD")?.name ?: error("HEAD commit not found")
        RepositoryMetadata(
            revision = head,
            branch = git.repository.fullBranch?.removePrefix("refs/heads/")?.takeUnless { it == head },
            defaultBranch = git.repository.findRef("refs/remotes/origin/HEAD")
                ?.target?.name?.removePrefix("refs/remotes/origin/"),
        )
    }

    override fun resolveTags(prefix: String, tagLookup: TagLookup): TagSelection = withGit { git ->
        val head = git.repository.resolve("HEAD")?.name ?: error("HEAD commit not found")
        val names = git.tagList().call().filter { ref ->
            val peeled = git.repository.refDatabase.peel(ref)
            (peeled.peeledObjectId ?: peeled.objectId).name == head
        }.map { it.name.removePrefix(Constants.R_TAGS) }
        val exact = highestVersionTag(names, prefix)
        val base = exact ?: when (tagLookup) {
            TagLookup.NEAREST_REACHABLE -> nearestVersionTag(git, prefix)
            TagLookup.HIGHEST_REACHABLE -> highestReachableVersionTag(git, prefix)
            TagLookup.HIGHEST_GLOBAL -> highestVersionTag(git, prefix)
        }
        TagSelection(exact?.name, base?.name)
    }

    private fun highestVersionTag(git: Git, prefix: String): VersionTag? {
        val tags = git.tagList().call().map { it.name.removePrefix(Constants.R_TAGS) }
        return highestVersionTag(tags, prefix)
    }

    private fun highestReachableVersionTag(git: Git, prefix: String): VersionTag? = RevWalk(git.repository).use { walk ->
        val head = walk.parseCommit(git.repository.resolve("HEAD"))
        val names = git.tagList().call().filter { ref ->
            val peeled = git.repository.refDatabase.peel(ref)
            val target = walk.parseAny(peeled.peeledObjectId ?: peeled.objectId)
            target is RevCommit && walk.isMergedInto(target, head)
        }.map { it.name.removePrefix(Constants.R_TAGS) }
        highestVersionTag(names, prefix)
    }

    private fun nearestVersionTag(git: Git, prefix: String): VersionTag? {
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
