package pl.allegro.tech.build.axion.release.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.TagOpt
import org.gradle.api.logging.Logging

internal object ShallowFetchStrategy {

    private val logger = Logging.getLogger(ShallowFetchStrategy::class.java)

    /**
     * Exponentially deepens a shallow clone until git-describe can resolve a tag matching
     * [tagPrefix], then gives up and fetches the full history as a last resort.
     *
     * Depth sequence: 1, 2, 4, 8, 16, 32, 64, 128, 256, 512 → unshallow.
     */
    fun deepen(git: Git, tagPrefix: String, remote: String) {
        var depth = 1
        repeat(10) {
            logger.lifecycle("[axion-release] Shallow clone detected — fetching history (depth=$depth) from $remote")
            runCatching {
                git.fetch()
                    .setRemote(remote)
                    .setDepth(depth)
                    .setTagOpt(TagOpt.FETCH_TAGS)
                    .call()
            }.onFailure { e ->
                logger.warn("[axion-release] Shallow fetch failed at depth $depth: ${e.message}")
                return
            }

            val found = runCatching {
                git.describe().setLong(true).setMatch("$tagPrefix*").call()
            }.getOrNull() != null

            if (found) {
                logger.lifecycle("[axion-release] Tag found after deepening to depth=$depth")
                return
            }
            depth *= 2
        }

        // Depth 512 still not enough — fetch the entire history
        logger.lifecycle("[axion-release] Tag not found within depth ${depth / 2}, unshallowing fully from $remote")
        runCatching {
            git.fetch()
                .setRemote(remote)
                .setUnshallow(true)
                .setTagOpt(TagOpt.FETCH_TAGS)
                .call()
        }.onFailure { e ->
            logger.warn("[axion-release] Full unshallow failed: ${e.message}")
        }
    }
}
