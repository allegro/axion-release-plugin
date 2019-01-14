package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.scm.TaggedCommits

import java.util.regex.Pattern

/**
 * Contains logic of sorting out which version should be used when there are multiple
 * versions available.
 *
 * Precedence:
 *
 * a) when on head
 * * highest stable
 * * alpha
 *
 * b) when not on head:
 * * any highest (stable or alpha)
 */
class VersionSorter {

    Map pickTaggedCommit(TaggedCommits taggedCommits,
                         boolean ignoreNextVersionTags,
                         boolean forceSnapshot,
                         Pattern nextVersionTagPattern,
                         VersionFactory versionFactory) {
        Set<Version> versions = []
        Map<Version, Boolean> isVersionNextVersion = [:]
        Map<Version, TagsOnCommit> versionToCommit = new LinkedHashMap<>()

        for (TagsOnCommit tagsEntry : taggedCommits.getCommits()) {
            List<String> tags = tagsEntry.tags

            // next version should be ignored when tag is on head
            // and there are other, normal tags on it
            // because when on single commit on head - normal ones have precedence
            // however, we should take into account next version
            // in case of forced snapshot
            boolean ignoreNextVersionOnHead = taggedCommits.isLatestCommit(tagsEntry.commitId) &&
                !tagsEntry.hasOnlyMatching(nextVersionTagPattern) &&
                !forceSnapshot

            for (String tag : tags) {
                boolean isNextVersion = tag ==~ nextVersionTagPattern
                if (isNextVersion && (ignoreNextVersionTags || ignoreNextVersionOnHead)) {
                    continue
                }

                Version version = versionFactory.versionFromTag(tag)
                boolean versionDidNotExist = versions.add(version)
                boolean isNormalVersion = !isNextVersion
                // normal tags have precedence over nextVersion tags with same version
                // if normal tag already exists, nextVersion will be discarded
                // if nextVersion already exists, normal tag will overwrite it
                if (versionDidNotExist || isNormalVersion) {
                    versionToCommit.put(version, tagsEntry)
                }

                if (isVersionNextVersion.containsKey(version)) {
                    isVersionNextVersion[version] = isVersionNextVersion[version] && isNextVersion
                } else {
                    isVersionNextVersion[version] = isNextVersion
                }
            }
        }

        List<Version> versionList = versions.asList()
        Collections.sort(versionList, Collections.reverseOrder())
        Version version = versionList[0] ?: versionFactory.initialVersion()

        TagsOnCommit versionCommit = versionToCommit.get(version)

        return [
            version      : version,
            isNextVersion: isVersionNextVersion.containsKey(version) && isVersionNextVersion[version],
            noTagsFound  : versions.isEmpty(),
            commit       : versionCommit?.commitId
        ]
    }

}
