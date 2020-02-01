package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.logging.ReleaseLogger
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

class NextVersionMarker {

    private static final ReleaseLogger logger = ReleaseLogger.Factory.logger(NextVersionMarker)

    private final ScmService repositoryService
    private final List<String> dirsToExclude;

    NextVersionMarker(ScmService repositoryService) {
        this(repositoryService, Collections.emptyList());
    }

    NextVersionMarker(ScmService repositoryService, List<String> dirsToExclude) {
        this.repositoryService = repositoryService
        this.dirsToExclude = dirsToExclude;
    }

    void markNextVersion(String projectRootRelativePath, NextVersionProperties nextVersionRules, TagProperties tagRules, VersionConfig versionConfig) {

        String nextVersion = null
        if (nextVersionRules.nextVersion) {
            nextVersion = nextVersionRules.nextVersion
        } else {
            Version currentVersion = Version.valueOf(versionConfig.undecoratedVersion)
            VersionIncrementerContext context = new VersionIncrementerContext(currentVersion, repositoryService.position())
            nextVersion = nextVersionRules.versionIncrementer ?
                PredefinedVersionIncrementer.versionIncrementerFor(nextVersionRules.versionIncrementer)(context) :
                versionConfig.versionIncrementer(context)
            logger.info("Next Version not specified. Creating next version with default incrementer: $nextVersion")
        }

        String tagName = tagRules.serialize(tagRules, nextVersion.toString())
        String nextVersionTag = nextVersionRules.serializer(nextVersionRules, tagName)

        logger.quiet("Creating next version marker tag: $nextVersionTag")
        repositoryService.tag(repositoryService.positionOfLastChangeIn(projectRootRelativePath, dirsToExclude).getRevision(), nextVersionTag)
        repositoryService.push()
    }
}
