package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.domain.scm.TagsOnCommit
import pl.allegro.tech.build.axion.release.infrastructure.git.GitRepository

import java.util.regex.Pattern

class VersionResolver {

    private final ScmRepository repository

    VersionResolver(ScmRepository repository) {
        this.repository = repository
    }

    VersionContext resolveVersion(VersionProperties versionRules, TagProperties tagProperties, NextVersionProperties nextVersionProperties) {
        ScmPosition position = new ScmPosition(repository.currentBranch())

        VersionFactory versionFactory = new VersionFactory(versionRules, tagProperties, nextVersionProperties, position)

        Map versions = readVersions(versionFactory, tagProperties, nextVersionProperties)

        ScmState scmState = new ScmState(
                versions.onReleaseTag,
                versions.onNextVersionTag,
                versions.noTagsFound,
                repository.checkUncommittedChanges()
        )

        Map finalVersion = versionFactory.createFinalVersion(scmState, versions.current)

        return new VersionContext(finalVersion.version, finalVersion.snapshot, versions.previous, position)
    }

    private Map readVersions(VersionFactory versionFactory,
                             TagProperties tagProperties,
                             NextVersionProperties nextVersionProperties) {
        GitRepository repository = (GitRepository) this.repository
        Pattern releaseTagPattern = ~/^${tagProperties.prefix}.*/
        Pattern nextVersionTagPattern = ~/.*${nextVersionProperties.suffix}$/

        TagsOnCommit tags = repository.latestTags(releaseTagPattern)
        TagsOnCommit previousTags = tags;

        Map currentVersionInfo = versionFromTags(tags.tags, false, nextVersionTagPattern, versionFactory)
        Version currentVersion = currentVersionInfo.version


        while (previousTags.hasOnlyMatching(nextVersionTagPattern)) {
            previousTags = repository.latestTags(releaseTagPattern, previousTags.commitId)
        }
        Version previousVersion = versionFromTags(previousTags.tags, true, nextVersionTagPattern, versionFactory).version

        return [
                current         : currentVersion,
                previous        : previousVersion,
                onReleaseTag    : tags.isHead && !currentVersionInfo.isNextVersion,
                onNextVersionTag: currentVersionInfo.isNextVersion,
                noTagsFound     : currentVersionInfo.noTagsFound
        ]
    }

    private Map versionFromTags(List<String> tags,
                                boolean ignoreNextVersionTags,
                                Pattern nextVersionTagPattern,
                                VersionFactory versionFactory) {
        List<Version> versions = []
        Map<Version, Boolean> isVersionNextVersion = [:].withDefault({false})
        for (String tag : tags) {
            boolean isNextVersion = tag ==~ nextVersionTagPattern
            if (ignoreNextVersionTags && isNextVersion) {
                continue
            }

            Version version = versionFactory.versionFromTag(tag)
            versions.add(version)
            isVersionNextVersion[version] = isNextVersion
        }

        Collections.sort(versions, Collections.reverseOrder())
        Version version = versions.isEmpty() ? versionFactory.initialVersion() : versions[0]

        return [
                version      : version,
                isNextVersion: isVersionNextVersion.get(version),
                noTagsFound  : versions.isEmpty()
        ]
    }
}
