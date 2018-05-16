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

    VersionResolver(ScmRepository repository) {
        this.repository = repository
    }

    VersionContext resolveVersion(VersionProperties versionRules, TagProperties tagProperties, NextVersionProperties nextVersionProperties) {
        ScmPosition position = repository.currentPosition()

        VersionFactory versionFactory = new VersionFactory(versionRules, tagProperties, nextVersionProperties, position)

        Map versions
        if (versionFactory.versionProperties.useHighestVersion) {
            versions = readVersionsByHighestVersion(versionFactory, tagProperties, nextVersionProperties)
        } else {
            versions = readVersions(versionFactory, tagProperties, nextVersionProperties)
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
                             NextVersionProperties nextVersionProperties) {
        Pattern releaseTagPattern = ~/^${tagProperties.prefix}.*/
        Pattern nextVersionTagPattern = ~/.*${nextVersionProperties.suffix}$/

        Map currentVersionInfo, previousVersionInfo
        TagsOnCommit latestTags = repository.latestTags(releaseTagPattern)
        currentVersionInfo = versionFromTaggedCommits([latestTags], false, nextVersionTagPattern, versionFactory)

        TagsOnCommit previousTags = latestTags
        while (previousTags.hasOnlyMatching(nextVersionTagPattern)) {
            previousTags = repository.latestTags(releaseTagPattern, previousTags.commitId)
        }
        previousVersionInfo = versionFromTaggedCommits([previousTags], true, nextVersionTagPattern, versionFactory)

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
                                             NextVersionProperties nextVersionProperties) {
        Pattern releaseTagPattern = ~/^${tagProperties.prefix}.*/
        Pattern nextVersionTagPattern = ~/.*${nextVersionProperties.suffix}$/

        Map currentVersionInfo, previousVersionInfo
        List<TagsOnCommit> allTaggedCommits = repository.taggedCommits(releaseTagPattern)

        currentVersionInfo = versionFromTaggedCommits(allTaggedCommits, false, nextVersionTagPattern, versionFactory)
        previousVersionInfo = versionFromTaggedCommits(allTaggedCommits, true, nextVersionTagPattern, versionFactory)

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
                                         VersionFactory versionFactory) {
        Set<Version> versions = []
        Map<Version, Boolean> isVersionNextVersion = [:]
        Map<Version, TagsOnCommit> versionToCommit = new LinkedHashMap<>()

        for (TagsOnCommit tagsEntry : taggedCommits) {
            List<String> tags = tagsEntry.tags
            for (String tag : tags) {
                boolean isNextVersion = tag ==~ nextVersionTagPattern
                if (ignoreNextVersionTags && isNextVersion) {
                    continue
                }

                Version version = versionFactory.versionFromTag(tag)
                // normal tags have precedence over nextVersion
                // if normal tag already exists, nextVersion will be discarded
                // if nextVersion already exists, normal tag will overwrite it
                if (versions.add(version) || !isNextVersion) {
                    versionToCommit.put(version, tagsEntry)
                }

                if (isVersionNextVersion.containsKey(version)) {
                    isVersionNextVersion[version] = isVersionNextVersion[version] && isNextVersion
                } else {
                    isVersionNextVersion[version] = isNextVersion
                }
            }
        }

        def versionList = versions.asList()
        Collections.sort(versionList, Collections.reverseOrder())
        
        Version version = versionList.find { !isVersionNextVersion[it] } ?: versionList[0] ?: versionFactory.initialVersion()

        TagsOnCommit versionCommit = versionToCommit.get(version)
        return [
            version      : version,
            isNextVersion: isVersionNextVersion.containsKey(version) && isVersionNextVersion[version],
            noTagsFound  : versions.isEmpty(),
            commit       : versionCommit?.commitId,
            isHead       : versionCommit?.isHead
        ]
    }
}
