package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.domain.scm.TaggedCommits
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
    // TODO Convert to Optional<String> ?!
    private String projectRootRelativePath

    VersionResolver(ScmRepository repository) {
        this(repository, null)
    }

    VersionResolver(ScmRepository repository, String projectRootRelativePath) {
        this.repository = repository
        this.projectRootRelativePath = projectRootRelativePath
        this.sorter = new VersionSorter()
    }

    VersionContext resolveVersion(VersionProperties versionRules, TagProperties tagProperties, NextVersionProperties nextVersionProperties) {
        ScmPosition latestChangePosition
        if (projectRootRelativePath) {
            latestChangePosition = repository.currentPosition(projectRootRelativePath)
        } else {
            latestChangePosition = repository.currentPosition()
        }

        VersionFactory versionFactory = new VersionFactory(versionRules, tagProperties, nextVersionProperties, latestChangePosition)

        Map versions
        if (versionFactory.versionProperties.useHighestVersion) {
            versions = readVersionsByHighestVersion(versionFactory, tagProperties, nextVersionProperties, versionRules, latestChangePosition)
        } else {
            versions = readVersions(versionFactory, tagProperties, nextVersionProperties, versionRules, latestChangePosition)
        }

        ScmState scmState = new ScmState(
            versions.onReleaseTag,
            versions.onNextVersionTag,
            versions.noTagsFound,
            repository.checkUncommittedChanges()
        )

        Map finalVersion = versionFactory.createFinalVersion(scmState, versions.current)

        return new VersionContext(finalVersion.version, finalVersion.snapshot, versions.previous, latestChangePosition)
    }

    private Map readVersions(VersionFactory versionFactory,
                             TagProperties tagProperties,
                             NextVersionProperties nextVersionProperties,
                             VersionProperties versionProperties,
                             ScmPosition latestChangePosition) {

        Pattern releaseTagPattern = ~/^${tagProperties.prefix}.*/
        Pattern nextVersionTagPattern = ~/.*${nextVersionProperties.suffix}$/
        boolean forceSnapshot = versionProperties.forceSnapshot

        Map currentVersionInfo, previousVersionInfo
        TagsOnCommit latestTags = repository.latestTags(releaseTagPattern)
        TaggedCommits latestTaggedCommit = new TaggedCommits([latestTags], latestChangePosition.revision)
        currentVersionInfo = versionFromTaggedCommits(latestTaggedCommit, false, nextVersionTagPattern,
            versionFactory, forceSnapshot)
        boolean onCommitWithLatestChange = currentVersionInfo.commit == latestChangePosition.revision

        TagsOnCommit previousTags = latestTags
        while (previousTags.hasOnlyMatching(nextVersionTagPattern)) {
            previousTags = repository.latestTags(releaseTagPattern, previousTags.commitId)
        }
        TaggedCommits previousTaggedCommit = new TaggedCommits([previousTags], latestChangePosition.revision)
        previousVersionInfo = versionFromTaggedCommits(previousTaggedCommit, true, nextVersionTagPattern,
            versionFactory, forceSnapshot)

        Version currentVersion = currentVersionInfo.version
        Version previousVersion = previousVersionInfo.version
        return [
            current         : currentVersion,
            previous        : previousVersion,
            onReleaseTag    : onCommitWithLatestChange && !currentVersionInfo.isNextVersion,
            onNextVersionTag: currentVersionInfo.isNextVersion,
            noTagsFound     : currentVersionInfo.noTagsFound
        ]
    }

    private Map readVersionsByHighestVersion(VersionFactory versionFactory,
                                             TagProperties tagProperties,
                                             NextVersionProperties nextVersionProperties,
                                             VersionProperties versionProperties,
                                             ScmPosition latestChangePosition) {

        Pattern releaseTagPattern = ~/^${tagProperties.prefix}.*/
        Pattern nextVersionTagPattern = ~/.*${nextVersionProperties.suffix}$/
        boolean forceSnapshot = versionProperties.forceSnapshot

        Map currentVersionInfo, previousVersionInfo
        TaggedCommits allTaggedCommits = new TaggedCommits(repository.taggedCommits(releaseTagPattern), latestChangePosition.revision);

        currentVersionInfo = versionFromTaggedCommits(allTaggedCommits, false, nextVersionTagPattern,
            versionFactory, forceSnapshot)
        previousVersionInfo = versionFromTaggedCommits(allTaggedCommits, true, nextVersionTagPattern,
            versionFactory, forceSnapshot)

        Version currentVersion = currentVersionInfo.version
        Version previousVersion = previousVersionInfo.version
        return [
            current         : currentVersion,
            previous        : previousVersion,
            onReleaseTag    : (currentVersionInfo.commit == latestChangePosition.revision) && !currentVersionInfo.isNextVersion,
            onNextVersionTag: currentVersionInfo.isNextVersion,
            noTagsFound     : currentVersionInfo.noTagsFound
        ]
    }

    private Map versionFromTaggedCommits(TaggedCommits taggedCommits,
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
