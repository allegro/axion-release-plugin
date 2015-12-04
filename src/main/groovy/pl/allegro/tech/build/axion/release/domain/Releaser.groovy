package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHooksRunner
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

class Releaser {

    private final Logger logger = LoggerFactory.getLogger(ReleaseHooksRunner)

    private final ScmService repository

    private final ReleaseHooksRunner hooksRunner

    private Project project

    Releaser(ScmService repository, ReleaseHooksRunner hooksRunner, Project project) {
        this.repository = repository
        this.hooksRunner = hooksRunner
        this.project = project
    }

    void release(VersionConfig versionConfig) {
        VersionWithPosition positionedVersion = versionConfig.getRawVersion()
        Version version = positionedVersion.version

        if (notOnTagAlready(positionedVersion) || VersionReadOptions.fromProject(project, versionConfig).forceVersion) {
            String tagName = versionConfig.tag.serialize(versionConfig.tag, version.toString())

            if (versionConfig.createReleaseCommit) {
                logger.trace("Creating release commit")
                versionConfig.hooks.pre('commit', versionConfig.releaseCommitMessage)
            }

            hooksRunner.runPreReleaseHooks(positionedVersion, version)

            logger.trace("Creating tag: $tagName")
            repository.tag(tagName)

            hooksRunner.runPostReleaseHooks(positionedVersion, version)
        } else {
            logger.trace("Working on released version ${versionConfig.version}, nothing to release.")
        }
    }

    void pushRelease() {
        repository.push()
    }

    private boolean notOnTagAlready(VersionWithPosition positionedVersion) {
        return positionedVersion.snapshotVersion
    }
}
