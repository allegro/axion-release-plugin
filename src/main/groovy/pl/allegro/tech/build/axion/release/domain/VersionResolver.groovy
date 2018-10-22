package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.domain.scm.TagsOnCommit

import java.util.regex.Pattern

/**
 * Returned structure is:
 * * previousVersion: version read from last release tag
 * * version: either:
 *   * forced version
 *   * version read from last next version tag
 *   * version read from last release tag and incremented when not on tag
 *   * version read from last release tag when on tag
 */
class VersionResolver {

    private final ScmRepository repository

    private final VersionSorter sorter

    VersionResolver(ScmRepository repository) {
        this.repository = repository
        this.sorter = new VersionSorter()
    }

    VersionContext resolveVersion(VersionProperties versionRules, TagProperties tagProperties, NextVersionProperties nextVersionProperties) {
        ScmPosition position = repository.currentPosition()

        VersionFactory versionFactory = new VersionFactory(versionRules, tagProperties, nextVersionProperties, position)

        Map versions
        if (versionFactory.versionProperties.useHighestVersion) {
            versions = readVersionsByHighestVersion(versionFactory, tagProperties, nextVersionProperties, versionRules)
        } else {
            versions = readVersions(versionFactory, tagProperties, nextVersionProperties, versionRules)
        }

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
                             NextVersionProperties nextVersionProperties,
                             VersionProperties versionProperties) {

        Pattern releaseTagPattern = ~/^${tagProperties.prefix}.*/
        Pattern nextVersionTagPattern = ~/.*${nextVersionProperties.suffix}$/
        boolean forceSnapshot = versionProperties.forceSnapshot

        Map currentVersionInfo, previousVersionInfo
        TagsOnCommit latestTags = repository.latestTags(releaseTagPattern)
        currentVersionInfo = versionFromTaggedCommits([latestTags], false, nextVersionTagPattern,
            versionFactory, forceSnapshot)

        TagsOnCommit previousTags = latestTags
        while (previousTags.hasOnlyMatching(nextVersionTagPattern)) {
            previousTags = repository.latestTags(releaseTagPattern, previousTags.commitId)
        }
        previousVersionInfo = versionFromTaggedCommits([previousTags], true, nextVersionTagPattern,
            versionFactory, forceSnapshot)

        Version currentVersion = currentVersionInfo.version
        Version previousVersion = previousVersionInfo.version
        return [
            current         : currentVersion,
            previous        : previousVersion,
            onReleaseTag    : currentVersionInfo.isHead && !currentVersionInfo.isNextVersion,
            onNextVersionTag: currentVersionInfo.isNextVersion,
            noTagsFound     : currentVersionInfo.noTagsFound
        ]
    }

    private Map readVersionsByHighestVersion(VersionFactory versionFactory,
                                             TagProperties tagProperties,
                                             NextVersionProperties nextVersionProperties,
                                             VersionProperties versionProperties) {

        Pattern releaseTagPattern = ~/^${tagProperties.prefix}.*/
        Pattern nextVersionTagPattern = ~/.*${nextVersionProperties.suffix}$/
        boolean forceSnapshot = versionProperties.forceSnapshot

        Map currentVersionInfo, previousVersionInfo
        List<TagsOnCommit> allTaggedCommits = repository.taggedCommits(releaseTagPattern)

        currentVersionInfo = versionFromTaggedCommits(allTaggedCommits, false, nextVersionTagPattern,
            versionFactory, forceSnapshot)
        previousVersionInfo = versionFromTaggedCommits(allTaggedCommits, true, nextVersionTagPattern,
            versionFactory, forceSnapshot)

        Version currentVersion = currentVersionInfo.version
        Version previousVersion = previousVersionInfo.version
        return [
            current         : currentVersion,
            previous        : previousVersion,
            onReleaseTag    : currentVersionInfo.isHead && !currentVersionInfo.isNextVersion,
            onNextVersionTag: currentVersionInfo.isNextVersion,
            noTagsFound     : currentVersionInfo.noTagsFound
        ]
    }

    private Map versionFromTaggedCommits(List<TagsOnCommit> taggedCommits,
                                         boolean ignoreNextVersionTags,
                                         Pattern nextVersionTagPattern,
                                         VersionFactory versionFactory,
                                         boolean forceSnapshot) {

        return sorter.pickTaggedCommit(
            taggedCommits,
            ignoreNextVersionTags,
            forceSnapshot,
            nextVersionTagPattern,
            versionFactory)
    }
}
