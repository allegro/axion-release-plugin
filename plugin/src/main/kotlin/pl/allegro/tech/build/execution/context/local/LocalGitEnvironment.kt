package pl.allegro.tech.build.execution.context.local

import pl.allegro.tech.build.execution.context.VersionTag
import pl.allegro.tech.build.execution.context.highestVersionTag
import pl.allegro.tech.build.execution.context.versionTags
import pl.allegro.tech.build.extension.GitSpec
import java.io.File

internal class LocalGitEnvironment(
    internal val options: GitSpec,
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

    override val defaultBranchName: String
        get() = runGit("symbolic-ref", "--short", "refs/remotes/origin/HEAD").substringAfterLast('/')

    override val currentBranchName: String
        get() = runGit("branch", "--show-current")

    override val headCommit: String
        get() = runGit("rev-parse", "HEAD")

    override fun highestVersionTag(prefix: String): VersionTag? {
        val tags = runGit("tag", "--list").lineSequence().filter(String::isNotBlank).toList()
        return highestVersionTag(tags, prefix)
    }

    override fun nearestVersionTag(prefix: String): VersionTag? {
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

    private fun runGit(vararg arguments: String): String {
        val process = ProcessBuilder(listOf("git") + arguments)
            .directory(directory)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw IllegalStateException("git ${arguments.joinToString(" ")} failed ($exitCode): $output")
        }
        return output
    }
}
