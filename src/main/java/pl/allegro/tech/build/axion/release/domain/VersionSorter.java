package pl.allegro.tech.build.axion.release.domain;

import com.github.zafarkhaja.semver.Version;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import pl.allegro.tech.build.axion.release.domain.scm.TaggedCommits;
import pl.allegro.tech.build.axion.release.domain.scm.TagsOnCommit;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Contains logic of sorting out which version should be used when there are multiple
 * versions available.
 * <p>
 * Precedence:
 * <p>
 * a) when on head
 * * highest stable
 * * alpha
 * <p>
 * b) when not on head:
 * * any highest (stable or alpha)
 */
class VersionSorter {
    private static final Logger logger = Logging.getLogger(VersionSorter.class);

    Result pickTaggedCommit(
        TaggedCommits taggedCommits,
        boolean ignoreNextVersionTags,
        boolean forceSnapshot,
        Pattern nextVersionTagPattern,
        VersionFactory versionFactory
    ) {
        Set<Version> versions = new LinkedHashSet<>();
        LinkedHashMap<Version, Boolean> isVersionNextVersion = new LinkedHashMap<>();
        LinkedHashMap<Version, TagsOnCommit> versionToCommit = new LinkedHashMap<>();
        boolean tagsFound = false;

        for (TagsOnCommit tagsEntry : taggedCommits.getCommits()) {
            List<String> tags = tagsEntry.getTags();
            tagsFound = tagsFound || !tags.isEmpty();

            // next version should be ignored when tag is on head
            // and there are other, normal tags on it
            // because when on single commit on head - normal ones have precedence
            // however, we should take into account next version
            // in case of forced snapshot
            boolean ignoreNextVersionOnHead = taggedCommits.isLatestCommit(tagsEntry.getCommitId())
                && !tagsEntry.hasOnlyMatching(nextVersionTagPattern) && !forceSnapshot;

            for (String tag : tags) {
                boolean isNextVersion = nextVersionTagPattern.matcher(tag).matches();
                if (isNextVersion && (ignoreNextVersionTags || ignoreNextVersionOnHead)) {
                    logger.debug("Ignoring tag: {}, because it's a next version tag and it's not forced", tag);
                    continue;
                }

                Version version = versionFactory.versionFromTag(tag);
                logger.debug("Detected version: {} from tag: {}", version, tag);

                boolean versionDidNotExist = versions.add(version);
                boolean isNormalVersion = !isNextVersion;
                // normal tags have precedence over nextVersion tags with same version
                // if normal tag already exists, nextVersion will be discarded
                // if nextVersion already exists, normal tag will overwrite it
                if (versionDidNotExist || isNormalVersion) {
                    versionToCommit.put(version, tagsEntry);
                }

                if (isVersionNextVersion.containsKey(version)) {
                    isVersionNextVersion.put(version, isVersionNextVersion.get(version) && isNextVersion);
                } else {
                    isVersionNextVersion.put(version, isNextVersion);
                }

            }
        }
        if (!tagsFound) {
            logger.quiet("No tags were found in git history");
        }

        Version version = findBestVersion(versionFactory, versions);
        logger.debug("Version: {}, was selected from versions: {}", version, versions);

        TagsOnCommit versionCommit = versionToCommit.get(version);

        return new Result(
            version,
            (isVersionNextVersion.containsKey(version) && isVersionNextVersion.get(version)),
            versions.isEmpty(),
            (versionCommit == null ? null : versionCommit.getCommitId())
        );
    }

    private static Version findBestVersion(VersionFactory versionFactory, Set<Version> versions) {
        List<Version> versionList = new ArrayList<>(versions);
        versionList.sort(Collections.reverseOrder());

        if (versionList.isEmpty()) {
            return versionFactory.initialVersion();
        }
        Version version = versionList.get(0);
        return version != null ? version : versionFactory.initialVersion();
    }

    static class Result {
        final Version version;
        final boolean isNextVersion;
        final boolean noTagsFound;
        final String commit;

        private Result(Version version, boolean isNextVersion, boolean noTagsFound, String commit) {
            this.version = version;
            this.isNextVersion = isNextVersion;
            this.noTagsFound = noTagsFound;
            this.commit = commit;
        }

        boolean isSameCommit(String otherCommitId) {
            return Objects.equals(commit, otherCommitId);
        }
    }
}
