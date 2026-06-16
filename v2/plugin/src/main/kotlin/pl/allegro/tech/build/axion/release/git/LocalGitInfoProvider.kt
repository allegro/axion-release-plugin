package pl.allegro.tech.build.axion.release.git

import com.github.zafarkhaja.semver.Version
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevWalk
import org.gradle.api.logging.Logging
import java.io.File

class LocalGitInfoProvider {

    private val logger = Logging.getLogger(LocalGitInfoProvider::class.java)

    fun getInfo(
        projectDir: File,
        tagPrefix: String,
        branchOverride: String? = null,
        currentTagOverride: String? = null,
        remote: String? = null,
        useHighestVersion: Boolean = false
    ): ScmInfo? {
        val git = try {
            Git.open(projectDir)
        } catch (e: Exception) {
            logger.warn("[axion-release] Could not open Git repository at $projectDir: ${e.message}")
            return null
        }

        return git.use { g ->
            val repo = g.repository
            val branch = branchOverride ?: repo.branch
            val isShallow = File(repo.directory, "shallow").exists()
            val isDirty = !g.status().call().isClean
            val headId: ObjectId? = repo.resolve(Constants.HEAD)
            val revision = headId?.name ?: ""
            val shortRevision = if (revision.length >= 7) revision.substring(0, 7) else revision

            // Use git describe to find nearest tag matching our prefix and commits since it.
            // setLong(true) gives "tagName-N-gHASH" even on exact tag matches (N=0).
            var described = runCatching {
                g.describe().setLong(true).setTags(true).setMatch("$tagPrefix*").call()
            }.getOrNull()

            // Shallow clone with no visible tag — deepen exponentially until we find one
            if (described == null && isShallow && remote != null) {
                ShallowFetchStrategy.deepen(g, tagPrefix, remote)
                described = runCatching {
                    g.describe().setLong(true).setTags(true).setMatch("$tagPrefix*").call()
                }.getOrNull()
            }

            if (described == null) {
                return@use ScmInfo(
                    branch = branch,
                    currentTag = null,
                    latestTag = null,
                    commitsSinceTag = 0,
                    isShallow = isShallow,
                    isDirty = isDirty,
                    revision = revision,
                    shortRevision = shortRevision
                )
            }

            // Parse "tagName-N-gSHA" — split from right to handle dashes in the tag name itself
            val parts = described.split("-")
            val commitsSinceTagFromDescribe = parts[parts.size - 2].toInt()
            val nearestTag = parts.dropLast(2).joinToString("-")

            // useHighestVersion: scan all prefix-matching tags, pick the highest semver one.
            // This fixes upmerge scenarios where a merge from release/1.20.x into release/1.21.x
            // would otherwise make git describe return a 1.20.x tag as the nearest ancestor.
            val (latestTag, commitsSinceTag) = if (useHighestVersion && headId != null) {
                findHighestVersionTag(g, repo, headId, tagPrefix) ?: (nearestTag to commitsSinceTagFromDescribe)
            } else {
                nearestTag to commitsSinceTagFromDescribe
            }

            val currentTag = when {
                currentTagOverride != null -> currentTagOverride
                commitsSinceTag == 0 -> latestTag
                else -> null
            }

            ScmInfo(
                branch = branch,
                currentTag = currentTag,
                latestTag = latestTag,
                commitsSinceTag = commitsSinceTag,
                isShallow = isShallow,
                isDirty = isDirty,
                revision = revision,
                shortRevision = shortRevision
            )
        }
    }

    private fun findHighestVersionTag(
        git: Git,
        repo: org.eclipse.jgit.lib.Repository,
        headId: ObjectId,
        tagPrefix: String
    ): Pair<String, Int>? {
        // Collect all tags that match the prefix and parse as semver
        val candidates = git.tagList().call()
            .filter { ref -> ref.name.startsWith("refs/tags/$tagPrefix") }
            .mapNotNull { ref ->
                val tagName = ref.name.removePrefix("refs/tags/")
                val versionStr = tagName.removePrefix(tagPrefix)
                runCatching { tagName to Version.parse(versionStr) }.getOrNull()
            }

        if (candidates.isEmpty()) return null

        // Sort descending by semver and find the highest one that is an ancestor of HEAD
        val sorted = candidates.sortedByDescending { (_, v) -> v }

        RevWalk(repo).use { walk ->
            val head = walk.parseCommit(headId)
            for ((tagName, _) in sorted) {
                val tagObjectId = repo.resolve("refs/tags/$tagName") ?: continue
                // Peel annotated tags down to the commit object
                val tagCommit = runCatching {
                    val obj = walk.parseAny(tagObjectId)
                    // RevTag → peel to commit; RevCommit → use directly
                    walk.parseCommit(walk.peel(obj).id)
                }.getOrNull() ?: continue

                if (walk.isMergedInto(tagCommit, head)) {
                    // Count commits from tagCommit to HEAD
                    walk.reset()
                    walk.markStart(head)
                    walk.markUninteresting(tagCommit)
                    var count = 0
                    for (c in walk) {
                        count++
                        if (count > 10_000) break // guard against very long histories
                    }
                    return tagName to count
                }
            }
        }
        return null
    }
}
