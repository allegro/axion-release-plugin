package pl.allegro.tech.build.execution.local

import java.io.File
import pl.allegro.tech.build.extension.TagLookup
import pl.allegro.tech.build.repository.RepositoryMetadata
import pl.allegro.tech.build.repository.TagSelection
import pl.allegro.tech.build.versioning.VersionTag
import pl.allegro.tech.build.versioning.highestVersionTag
import pl.allegro.tech.build.versioning.versionTags

internal class LocalGitEnvironment(
    private val directory: File,
) : LocalRepository {
    companion object {
        fun isAvailable(path: String?): Boolean {
            if (path == null) return false
            val names = listOf("git", "git.exe", "git.cmd", "git.bat")
            return path.split(File.pathSeparatorChar).any { directory ->
                names.any { name ->
                    val executable = File(directory, name)
                    executable.isFile && executable.canExecute()
                }
            }
        }
    }

    override fun readMetadata(): RepositoryMetadata = RepositoryMetadata(
        revision = runGit("rev-parse", "HEAD"),
        branch = runGit("branch", "--show-current").takeIf(String::isNotBlank),
        defaultBranch = runGit("symbolic-ref", "--short", "refs/remotes/origin/HEAD", allowFailure = true)
            .removePrefix("origin/").takeIf(String::isNotBlank),
    )

    override fun resolveTags(prefix: String, tagLookup: TagLookup): TagSelection {
        val exact = highestVersionTag(runGit("tag", "--points-at", "HEAD").lines(), prefix)
        val base = exact ?: when (tagLookup) {
            TagLookup.NEAREST_REACHABLE -> nearestVersionTag(prefix)
            TagLookup.HIGHEST_REACHABLE -> highestReachableVersionTag(prefix)
            TagLookup.HIGHEST_GLOBAL -> highestVersionTag(prefix)
        }
        return TagSelection(exact?.name, base?.name)
    }

    private fun highestVersionTag(prefix: String): VersionTag? {
        val tags = runGit("tag", "--list").lineSequence().filter(String::isNotBlank).toList()
        return highestVersionTag(tags, prefix)
    }

    private fun highestReachableVersionTag(prefix: String): VersionTag? =
        highestVersionTag(runGit("tag", "--merged", "HEAD", "--list").lines(), prefix)

    private fun nearestVersionTag(prefix: String): VersionTag? {
        val reachableTags = runGit("tag", "--merged", "HEAD", "--list")
            .lineSequence()
            .filter(String::isNotBlank)
            .toList()
        val matchingTags = versionTags(reachableTags, prefix)
        if (matchingTags.isEmpty()) return null

        val arguments = buildList {
            addAll(listOf("describe", "--tags", "--abbrev=0"))
            matchingTags.forEach { tag ->
                add("--match")
                add(tag.name)
            }
        }
        val name = runGit(*arguments.toTypedArray())
        return matchingTags.firstOrNull { it.name == name }
    }

    private fun runGit(vararg arguments: String, allowFailure: Boolean = false): String {
        val process = ProcessBuilder(listOf("git") + arguments)
            .directory(directory)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            if (allowFailure) return ""
            throw IllegalStateException("git ${arguments.joinToString(" ")} failed ($exitCode): $output")
        }
        return output
    }
}
