package pl.allegro.tech.build.environment

import com.github.zafarkhaja.semver.Version

interface GitEnvironment {
    val defaultBranchName: String
    val currentBranchName: String
    val headCommit: String
    val currentVersion: Version
}
