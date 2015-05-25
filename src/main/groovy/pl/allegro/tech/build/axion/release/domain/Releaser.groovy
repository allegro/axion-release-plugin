package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHooksRunner
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

class Releaser {

    private final ScmService repository

    private final ReleaseHooksRunner hooksRunner

    private final LocalOnlyResolver localOnlyResolver

    private final Logger logger

    private Project project

    Releaser(ScmService repository, ReleaseHooksRunner hooksRunner, LocalOnlyResolver localOnlyResolver, Project project) {
        this.repository = repository
        this.hooksRunner = hooksRunner
        this.localOnlyResolver = localOnlyResolver
        this.project = project
        this.logger = project.logger
    }

    void release(VersionConfig versionConfig) {
        VersionWithPosition positionedVersion = versionConfig.getRawVersion()
        Version version = positionedVersion.version

        if (notOnTagAlready(positionedVersion) || VersionReadOptions.fromProject(project).forceVersion) {
            String tagName = versionConfig.tag.serialize(versionConfig.tag, version.toString())

            if (versionConfig.createReleaseCommit) {
                logger.quiet("Creating release commit")
                versionConfig.hooks.pre('commit', versionConfig.releaseCommitMessage)
            }

            hooksRunner.runPreReleaseHooks(positionedVersion, version)

            logger.quiet("Creating tag: $tagName")
            repository.tag(tagName)

            hooksRunner.runPostReleaseHooks(positionedVersion, version)
        } else {
            logger.quiet("Working on released version ${versionConfig.version}, nothing to release.")
        }
    }

    void pushRelease() {
        if (!localOnlyResolver.localOnly(repository.remoteAttached())) {
            repository.push()
        } else {
            logger.quiet("Changes made to local repository only")
        }

    }

    private boolean notOnTagAlready(VersionWithPosition positionedVersion) {
        return positionedVersion.snapshotVersion
    }
}
