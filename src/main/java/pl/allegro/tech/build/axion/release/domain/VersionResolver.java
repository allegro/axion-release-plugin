package pl.allegro.tech.build.axion.release.domain;

import com.github.zafarkhaja.semver.Version;
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties;
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties;
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository;
import pl.allegro.tech.build.axion.release.domain.scm.TaggedCommits;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

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

    public VersionResolver(ScmRepository repository, String projectRootRelativePath) {
        this.repository = repository;
        this.projectRootRelativePath = projectRootRelativePath;
        this.sorter = new VersionSorter();
    }

    public VersionContext resolveVersion(VersionProperties versionProperties, TagProperties tagProperties, NextVersionProperties nextVersionProperties) {
        ScmPosition latestChangePosition = repository.positionOfLastChangeIn(
            projectRootRelativePath, versionProperties.getMonorepoProperties().getDirsToExclude()
        );

        VersionFactory versionFactory = new VersionFactory(versionProperties, tagProperties, nextVersionProperties, latestChangePosition, repository.isLegacyDefTagnameRepo());

        VersionInfo versions;
        if (versionProperties.isUseHighestVersion()) {
            versions = readVersionsByHighestVersion(versionFactory, tagProperties, nextVersionProperties, versionProperties, latestChangePosition);
        } else {
            versions = readVersions(versionFactory, tagProperties, nextVersionProperties, versionProperties, latestChangePosition);
        }


        ScmState scmState = new ScmState(
            versions.onReleaseTag,
            versions.onNextVersionTag,
            versions.noTagsFound,
            repository.checkUncommittedChanges()
        );

        VersionFactory.FinalVersion finalVersion = versionFactory.createFinalVersion(scmState, versions.current);

        return new VersionContext(finalVersion.version, versions.previousTag, finalVersion.snapshot, versions.previous, latestChangePosition);
    }

    private VersionInfo readVersions(
        VersionFactory versionFactory,
        TagProperties tagProperties,
        NextVersionProperties nextVersionProperties,
        VersionProperties versionProperties,
        ScmPosition latestChangePosition
    ) {

        String releaseTagPatternString = tagProperties.getPrefix();
        if (!releaseTagPatternString.isEmpty()) {
            releaseTagPatternString += tagProperties.getVersionSeparator();
        }

        Pattern releaseTagPattern = Pattern.compile("^" + releaseTagPatternString + ".*");
        Pattern nextVersionTagPattern = Pattern.compile(".*" + nextVersionProperties.getSuffix() + "$");
        boolean forceSnapshot = versionProperties.isForceSnapshot();

        List<String> previousTag = new LinkedList<>();
        TaggedCommits latestTaggedCommit = TaggedCommits.fromLatestCommit(repository, releaseTagPattern, latestChangePosition, previousTag);
        VersionSorter.Result currentVersionInfo = versionFromTaggedCommits(
            latestTaggedCommit, false,
            nextVersionTagPattern,
            versionFactory,
            forceSnapshot
        );

        boolean onCommitWithLatestChange = currentVersionInfo.isSameCommit(latestChangePosition.getRevision());

        TaggedCommits previousTaggedCommit = TaggedCommits.fromLatestCommitBeforeNextVersion(repository, releaseTagPattern, nextVersionTagPattern, latestChangePosition);
        VersionSorter.Result previousVersionInfo = versionFromTaggedCommits(previousTaggedCommit, true, nextVersionTagPattern,
            versionFactory, forceSnapshot);

        Version currentVersion = currentVersionInfo.version;
        Version previousVersion = previousVersionInfo.version;

        return new VersionInfo(
            currentVersion,
            (!previousTag.isEmpty())? previousTag.iterator().next() : null,
            previousVersion,
            (onCommitWithLatestChange && !currentVersionInfo.isNextVersion),
            currentVersionInfo.isNextVersion,
            currentVersionInfo.noTagsFound
        );
    }

    private VersionInfo readVersionsByHighestVersion(
        VersionFactory versionFactory,
        final TagProperties tagProperties,
        final NextVersionProperties nextVersionProperties,
        VersionProperties versionProperties,
        ScmPosition latestChangePosition
    ) {

        Pattern releaseTagPattern = Pattern.compile("^" + tagProperties.getPrefix() + ".*");
        Pattern nextVersionTagPattern = Pattern.compile(".*" + nextVersionProperties.getSuffix() + "$");
        boolean forceSnapshot = versionProperties.isForceSnapshot();

        TaggedCommits allTaggedCommits = TaggedCommits.fromAllCommits(repository, releaseTagPattern, latestChangePosition);

        VersionSorter.Result currentVersionInfo = versionFromTaggedCommits(allTaggedCommits, false, nextVersionTagPattern, versionFactory, forceSnapshot);
        VersionSorter.Result previousVersionInfo = versionFromTaggedCommits(allTaggedCommits, true, nextVersionTagPattern, versionFactory, forceSnapshot);

        Version currentVersion = currentVersionInfo.version;
        Version previousVersion = previousVersionInfo.version;

        boolean onCommitWithLatestChange = currentVersionInfo.isSameCommit(latestChangePosition.getRevision());

        return new VersionInfo(
            currentVersion,
            null,
            previousVersion,
            (onCommitWithLatestChange && !currentVersionInfo.isNextVersion),
            currentVersionInfo.isNextVersion,
            currentVersionInfo.noTagsFound
        );
    }

    private VersionSorter.Result versionFromTaggedCommits(TaggedCommits taggedCommits, boolean ignoreNextVersionTags, Pattern nextVersionTagPattern, VersionFactory versionFactory, boolean forceSnapshot) {
        return sorter.pickTaggedCommit(taggedCommits, ignoreNextVersionTags, forceSnapshot, nextVersionTagPattern, versionFactory);
    }

    private static final class VersionInfo {
        final Version current;

        //previous tag inferred from the current tag
        //if current is '1.2.3', previous tag could be '1.2.2'
        final String previousTag;

        //previous version in the context of the 'next version marker' mechanism
        //Caveat: if 'next version marker' tag was not found then 'previous' will be equal to 'current' *and*
        //  'previous' will not be the same as 'previousTag'.
        final Version previous;

        final boolean onReleaseTag;
        final boolean onNextVersionTag;
        final boolean noTagsFound;

        VersionInfo(Version current, String previousTag, Version previous, boolean onReleaseTag, boolean onNextVersionTag, boolean noTagsFound) {
            this.current = current;
            this.previousTag = previousTag;
            this.previous = previous;
            this.onReleaseTag = onReleaseTag;
            this.onNextVersionTag = onNextVersionTag;
            this.noTagsFound = noTagsFound;
        }
    }
}
