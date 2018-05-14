package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.logging.ReleaseLogger
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

class NextVersionMarker {

    private static final ReleaseLogger logger = ReleaseLogger.Factory.logger(NextVersionMarker)

    private final ScmService repositoryService
    private final VersionConfig versionConfig

        NextVersionMarker(ScmService repositoryService, VersionConfig config) {
            this.repositoryService = repositoryService
            this.versionConfig = config
    }

    void markNextVersion(NextVersionProperties nextVersionRules, TagProperties tagRules) {

        def version = nextVersionRules.nextVersion
        def currentVersion = Version.valueOf(versionConfig.version)

        if (nextVersionRules.versionIncrementer){
            versionConfig.versionIncrementer(nextVersionRules.versionIncrementer)
        }

        if (!version) {
            def context = new VersionIncrementerContext(currentVersion, repositoryService.position())
            version = versionConfig.versionIncrementer(context)
            logger.info("Next Version not specified. Creating next version with default incrementer: $version")
        }

        String tagName = tagRules.serialize(tagRules, version.toString())
        String nextVersionTag = nextVersionRules.serializer(nextVersionRules, tagName)

        logger.quiet("Creating next version marker tag: $nextVersionTag")
        repositoryService.tag(nextVersionTag)
        repositoryService.push()
    }
}
