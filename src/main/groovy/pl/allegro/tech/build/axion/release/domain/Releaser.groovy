package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import org.gradle.api.logging.Logger
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository

class Releaser {

    private final ScmRepository repository

    private final LocalOnlyResolver localOnlyResolver

    private final Logger logger

    Releaser(ScmRepository repository, LocalOnlyResolver localOnlyResolver, Logger logger) {
        this.repository = repository
        this.localOnlyResolver = localOnlyResolver
        this.logger = logger
    }

    void release(VersionConfig versionConfig) {
        VersionWithPosition positionedVersion = versionConfig.getRawVersion()
        Version version = positionedVersion.version

        if (version.preReleaseVersion == VersionService.SNAPSHOT) {
            version = version.setPreReleaseVersion(null)
            String tagName = versionConfig.tag.serialize(versionConfig.tag, version.toString())

            if(versionConfig.createReleaseCommit) {
                logger.quiet("Creating release commit")
                repository.commit(versionConfig.releaseCommitMessage(version.toString(), positionedVersion.position))
            }

            logger.quiet("Creating tag: $tagName")
            repository.tag(tagName)

            if(!localOnlyResolver.localOnly(repository.remoteAttached(versionConfig.remote))) {
                logger.quiet("Pushing all to remote: ${versionConfig.remote}")
                repository.push(versionConfig.remote)
            }
            else {
                logger.quiet("Changes made to local repository only")
            }
        }
        else {
            logger.quiet("Working on released version ${versionConfig.version}, nothing to do here.")
        }
    }

}
