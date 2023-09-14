package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

class NextVersionMarker {

    private static final Logger logger = LoggerFactory.getLogger(NextVersionMarker)

    private final ScmService repositoryService

    NextVersionMarker(ScmService repositoryService) {
        this.repositoryService = repositoryService
    }

    void markNextVersion(NextVersionProperties nextVersionRules, TagProperties tagRules, VersionConfig versionConfig) {

        String nextVersion = null
        if (nextVersionRules.nextVersion) {
            nextVersion = nextVersionRules.nextVersion
        } else {
            Version currentVersion = Version.valueOf(versionConfig.undecoratedVersion)
            VersionIncrementerContext context = new VersionIncrementerContext(currentVersion, repositoryService.position(), repositoryService.isLegacyDefTagnameRepo())
            nextVersion = nextVersionRules.versionIncrementer ?
                PredefinedVersionIncrementer.versionIncrementerFor(nextVersionRules.versionIncrementer).apply(context) :
                versionConfig.versionIncrementer.get().apply(context)
            logger.info("Next Version not specified. Creating next version with default incrementer: $nextVersion")
        }

        String tagName = tagRules.serialize.apply(tagRules, nextVersion.toString())
        String nextVersionTag = nextVersionRules.serializer.apply(nextVersionRules, tagName)

        logger.quiet("Creating next version marker tag: $nextVersionTag")
        repositoryService.tag(nextVersionTag)
        repositoryService.push()
    }
}
