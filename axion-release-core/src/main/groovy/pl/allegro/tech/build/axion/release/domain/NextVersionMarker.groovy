package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.logging.ReleaseLogger
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

class NextVersionMarker {

    private static final ReleaseLogger logger = ReleaseLogger.Factory.logger(NextVersionMarker)

    private final ScmService repository

    NextVersionMarker(ScmService repository) {
        this.repository = repository
    }

    void markNextVersion(NextVersionProperties nextVersionRules, TagProperties tagRules) {
        if (nextVersionRules.nextVersion == null) {
            throw new IllegalArgumentException("No next version specified! Use -Prelease.version to set next version.")
        }

        String tagName = tagRules.serialize(tagRules, nextVersionRules.nextVersion)
        String nextVersionTag = nextVersionRules.serializer(nextVersionRules, tagName)

        logger.quiet("Creating next version marker tag: $nextVersionTag")
        repository.tag(nextVersionTag)
        repository.push()
    }
}
