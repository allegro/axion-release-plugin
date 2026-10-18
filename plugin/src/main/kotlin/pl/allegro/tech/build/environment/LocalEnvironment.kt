package pl.allegro.tech.build.environment

import org.eclipse.jgit.api.Git
import java.io.File
import java.io.IOException

class LocalEnvironment : GitEnvironment {
    private val git: Git

    init {
        try {
            Git.open(File(".")).use { git -> this.git = git }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override val defaultBranchName: String
        get() {
            val repository = git.repository
            val defaultBranch = repository.findRef("refs/remotes/origin/HEAD")?.target?.name
            return defaultBranch?.substringAfterLast("/") ?: throw RuntimeException("Default branch not found")
        }


    override val currentBranchName: String
        get() {
            try {
                return git.repository.getBranch()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

    override val headCommit: String
        get() {
            try {
                return git.repository.resolve("HEAD").toObjectId().getName()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

    override fun getTag(prefix: String): String {
        //TODO: this wont work with useHighestVersion
        return git.describe().setTags(true).setAbbrev(0).setMatch("$prefix*").call()
    }
}
