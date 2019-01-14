package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

import pl.allegro.tech.build.axion.release.domain.scm.TaggedCommits

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
    private ScmPosition latestChangePosition
    /**
     * This is the path of the project relative to the Git root.
     * If this path is not empty then it means that the project is running as a submodule of a parent project.
     */
    private String projectRootRelativePath

    VersionResolver(ScmRepository repository, String projectRootRelativePath) {
        this.repository = repository
        this.projectRootRelativePath = projectRootRelativePath
        this.sorter = new VersionSorter()
    }

    VersionContext resolveVersion(VersionProperties versionRules, TagProperties tagProperties, NextVersionProperties nextVersionProperties) {
        latestChangePosition = repository.positionOfLastChangeIn(projectRootRelativePath, versionRules.monorepoProperties.dirsToExclude)

        VersionFactory versionFactory = new VersionFactory(versionRules, tagProperties, nextVersionProperties, latestChangePosition)

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

        return new VersionContext(finalVersion.version, finalVersion.snapshot, versions.previous, latestChangePosition)
    }

    private Map readVersions(VersionFactory versionFactory,
                             TagProperties tagProperties,
                             NextVersionProperties nextVersionProperties,
                             VersionProperties versionProperties) {

        Pattern releaseTagPattern = ~/^${tagProperties.prefix}${tagProperties.prefix != '' ? tagProperties.versionSeparator : ''}.*/
        Pattern nextVersionTagPattern = ~/.*${nextVersionProperties.suffix}$/
        boolean forceSnapshot = versionProperties.forceSnapshot

        Map currentVersionInfo, previousVersionInfo
        TaggedCommits latestTaggedCommit = TaggedCommits.fromLatestCommit(repository, releaseTagPattern, latestChangePosition)
        currentVersionInfo = versionFromTaggedCommits(latestTaggedCommit, false, nextVersionTagPattern,
            versionFactory, forceSnapshot)
        boolean onCommitWithLatestChange = currentVersionInfo.commit == latestChangePosition.revision

        TaggedCommits previousTaggedCommit = TaggedCommits.fromLatestCommitBeforeNextVersion(repository, releaseTagPattern, nextVersionTagPattern, latestChangePosition)
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
                                             VersionProperties versionProperties) {

        Pattern releaseTagPattern = ~/^${tagProperties.prefix}${tagProperties.prefix != '' ? tagProperties.versionSeparator : ''}.*/
        Pattern nextVersionTagPattern = ~/.*${nextVersionProperties.suffix}$/
        boolean forceSnapshot = versionProperties.forceSnapshot

        Map currentVersionInfo, previousVersionInfo
        TaggedCommits allTaggedCommits = TaggedCommits.fromAllCommits(repository, releaseTagPattern, latestChangePosition)

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
