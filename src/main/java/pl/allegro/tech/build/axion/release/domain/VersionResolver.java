package pl.allegro.tech.build.axion.release.domain;

import com.github.zafarkhaja.semver.Version;
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties;
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties;
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository;
import pl.allegro.tech.build.axion.release.domain.scm.TaggedCommits;

import java.util.List;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

/**
 * Returned structure is:
 * * previousVersion: version read from last release tag
 * * version: either:
 * * forced version
 * * version read from last next version tag
 * * version read from last release tag and incremented when not on tag
 * * version read from last release tag when on tag
 */
public class VersionResolver {

    private final ScmRepository repository;
    private final VersionSorter sorter;

    /**
     * This is the path of the project relative to the Git root.
     * If this path is not empty then it means that the project is running as a submodule of a parent project.
     */
    private final String projectRootRelativePath;

    public VersionResolver(ScmRepository repository, String projectRootRelativePath, boolean verbose) {
        this.repository = repository;
        this.projectRootRelativePath = projectRootRelativePath;
        this.sorter = new VersionSorter(verbose);
    }

    public VersionContext resolveVersion(VersionProperties versionProperties, TagProperties tagProperties, NextVersionProperties nextVersionProperties) {
        ScmPosition latestChangePosition = repository.positionOfLastChangeIn(
            projectRootRelativePath, versionProperties.getMonorepoConfig().getProjectDirs().get(), versionProperties.getMonorepoConfig().getDependenciesDirs().get()
        );

        VersionFactory versionFactory = new VersionFactory(versionProperties, tagProperties, nextVersionProperties, latestChangePosition, repository.isLegacyDefTagnameRepo());

        VersionInfo versions = readVersions(versionFactory, tagProperties, nextVersionProperties, versionProperties, latestChangePosition);

        ScmState scmState = new ScmState(
            versions.onReleaseTag,
            versions.onNextVersionTag,
            versions.noTagsFound,
            repository.checkUncommittedChanges()
        );

        VersionFactory.FinalVersion finalVersion = versionFactory.createFinalVersion(scmState, versions.current);

        return new VersionContext(finalVersion.version, finalVersion.snapshot, versions.previous, latestChangePosition);
    }

    private VersionInfo readVersions(
        VersionFactory versionFactory,
        TagProperties tagProperties,
        NextVersionProperties nextVersionProperties,
        VersionProperties versionProperties,
        ScmPosition latestChangePosition
    ) {

        List<Pattern> releaseTagPatterns = tagProperties.getAllPrefixes().stream()
            .map(prefix -> prefix.isEmpty() ? "" : prefix + tagProperties.getVersionSeparator())
            .map(pattern -> Pattern.compile("^" + pattern + "(.+)\\.(.+)\\.(.+)"))
            .collect(toList());
        Pattern nextVersionTagPattern = Pattern.compile("(.+)\\.(.+)\\.(.+)" + nextVersionProperties.getSuffix() + "$");
        boolean forceSnapshot = versionProperties.isForceSnapshot();
        boolean useHighestVersion = versionProperties.isUseHighestVersion();

        TaggedCommits latestTaggedCommit;
        TaggedCommits previousTaggedCommit;
        if (useHighestVersion) {
            TaggedCommits allTaggedCommits = TaggedCommits.fromAllCommits(repository, releaseTagPatterns, latestChangePosition);
            latestTaggedCommit = allTaggedCommits;
            previousTaggedCommit = allTaggedCommits;
        } else {
            latestTaggedCommit = TaggedCommits.fromLatestCommit(repository, releaseTagPatterns, latestChangePosition);
            previousTaggedCommit = TaggedCommits.fromLatestCommitBeforeNextVersion(repository, releaseTagPatterns, nextVersionTagPattern, latestChangePosition);
        }

        VersionSorter.Result currentVersionInfo = versionFromTaggedCommits(latestTaggedCommit, false, nextVersionTagPattern, versionFactory, forceSnapshot);
        VersionSorter.Result previousVersionInfo = versionFromTaggedCommits(previousTaggedCommit, true, nextVersionTagPattern, versionFactory, forceSnapshot);

        Version currentVersion = currentVersionInfo.version;
        Version previousVersion = previousVersionInfo.version;

        boolean onLatestVersion;
        if (projectRootRelativePath.isEmpty()) {
            // Regular case, enough to test if its the same commit
            onLatestVersion = currentVersionInfo.isSameCommit(latestChangePosition.getRevision());
        } else {
            // Here we must check if there are git differences for the path example case path subProj1:
            // A(last changes in subProj1) -> B -> C(tag 1.3.0) -> D -> E(head)
            // Now if we test for anywhere from C to E we should get 1.3.0
            String tagCommitRevision = currentVersionInfo.commit != null ? currentVersionInfo.commit : "";
            onLatestVersion = repository.isIdenticalForPath(projectRootRelativePath, latestChangePosition.getRevision(), tagCommitRevision);
            for (String dependency : versionProperties.getMonorepoConfig().getDependenciesDirs().get()) {
                onLatestVersion = onLatestVersion && repository.isIdenticalForPath(dependency, latestChangePosition.getRevision(), tagCommitRevision);
            }
        }

        return new VersionInfo(
            currentVersion,
            previousVersion,
            (onLatestVersion && !currentVersionInfo.isNextVersion),
            currentVersionInfo.isNextVersion,
            currentVersionInfo.noTagsFound
        );

    }

    private VersionSorter.Result versionFromTaggedCommits(TaggedCommits taggedCommits, boolean ignoreNextVersionTags, Pattern nextVersionTagPattern, VersionFactory versionFactory, boolean forceSnapshot) {
        return sorter.pickTaggedCommit(taggedCommits, ignoreNextVersionTags, forceSnapshot, nextVersionTagPattern, versionFactory);
    }

    private static final class VersionInfo {
        final Version current;
        final Version previous;
        final boolean onReleaseTag;
        final boolean onNextVersionTag;
        final boolean noTagsFound;

        VersionInfo(Version current, Version previous, boolean onReleaseTag, boolean onNextVersionTag, boolean noTagsFound) {
            this.current = current;
            this.previous = previous;
            this.onReleaseTag = onReleaseTag;
            this.onNextVersionTag = onNextVersionTag;
            this.noTagsFound = noTagsFound;
        }
    }
}
