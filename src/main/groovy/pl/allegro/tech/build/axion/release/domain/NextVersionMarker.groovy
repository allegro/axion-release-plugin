package pl.allegro.tech.build.axion.release.domain

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

class NextVersionMarker {

    private final Logger logger = LoggerFactory.getLogger(NextVersionMarker)

    private final ScmService repository

    NextVersionMarker(ScmService repository) {
        this.repository = repository
    }

    void markNextVersion(VersionConfig versionConfig, String nextVersion) {
        String tagName = versionConfig.tag.serialize(versionConfig.tag, nextVersion)
        String nextVersionTag = versionConfig.nextVersion.serializer(versionConfig.nextVersion, tagName)

        logger.trace("Creating next version marker tag: $nextVersionTag")
        repository.tag(nextVersionTag)
        repository.push()
    }
}
