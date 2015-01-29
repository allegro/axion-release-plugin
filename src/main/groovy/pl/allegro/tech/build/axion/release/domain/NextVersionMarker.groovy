package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.logging.Logger
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

class NextVersionMarker {

    private final ScmService repository

    private final LocalOnlyResolver localOnlyResolver

    private final Logger logger

    NextVersionMarker(ScmService repository, LocalOnlyResolver localOnlyResolver, Logger logger) {
        this.repository = repository
        this.localOnlyResolver = localOnlyResolver
        this.logger = logger
    }

    void markNextVersion(VersionConfig versionConfig, String nextVersion) {
        String tagName = versionConfig.tag.serialize(versionConfig.tag, nextVersion)
        String nextVersionTag = versionConfig.nextVersion.serializer(versionConfig.nextVersion, tagName)

        logger.quiet("Creating next version marker tag: $nextVersionTag")
        repository.tag(nextVersionTag)

        if(!localOnlyResolver.localOnly(repository.remoteAttached())) {
            repository.push()
        }
        else {
            logger.quiet("Changes made to local repository only")
        }
    }
}
