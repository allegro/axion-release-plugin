package pl.allegro.tech.build.axion.release.domain.scm;

import java.io.File;
import java.util.Optional;
import java.util.Set;

public class ScmProperties {

    private final String type;
    private final File directory;
    private final String remote;
    private final boolean pushTagsOnly;
    private final boolean fetchTags;
    private final boolean attachRemote;
    private final String remoteUrl;
    private final String overriddenBranchName;
    private final Boolean overriddenIsClean;
    private final ScmIdentity identity;
    private final Boolean unshallowRepoOnCI;
    private final Set<String> releaseBranchNames;
    private final boolean releaseOnlyOnReleaseBranches;
    private final boolean ignoreGlobalGitConfig;

    public ScmProperties(
        String type,
        File directory,
        String remote,
        boolean pushTagsOnly,
        boolean fetchTags,
        boolean attachRemote,
        String remoteUrl,
        String overriddenBranchName,
        Boolean overriddenIsClean,
        ScmIdentity identity,
        Boolean unshallowRepoOnCI,
        Set<String> releaseBranchNames,
        boolean releaseOnlyOnReleaseBranches,
        boolean ignoreGlobalGitConfig
    ) {
        this.type = type;
        this.directory = directory;
        this.remote = remote;
        this.pushTagsOnly = pushTagsOnly;
        this.fetchTags = fetchTags;
        this.attachRemote = attachRemote;
        this.remoteUrl = remoteUrl;
        this.overriddenBranchName = overriddenBranchName;
        this.overriddenIsClean = overriddenIsClean;
        this.identity = identity;
        this.unshallowRepoOnCI = unshallowRepoOnCI;
        this.releaseBranchNames = releaseBranchNames;
        this.releaseOnlyOnReleaseBranches = releaseOnlyOnReleaseBranches;
        this.ignoreGlobalGitConfig = ignoreGlobalGitConfig;
    }

    public ScmPushOptions pushOptions() {
        return new ScmPushOptions(remote, pushTagsOnly);
    }

    public final String getType() {
        return type;
    }

    public final File getDirectory() {
        return directory;
    }

    public final String getRemote() {
        return remote;
    }

    public final boolean isPushTagsOnly() {
        return pushTagsOnly;
    }

    public final boolean isFetchTags() {
        return fetchTags;
    }

    public final boolean isAttachRemote() {
        return attachRemote;
    }

    public final String getRemoteUrl() {
        return remoteUrl;
    }

    public String getOverriddenBranchName() {
        return overriddenBranchName;
    }

    public Optional<Boolean> getOverriddenIsClean() {
        return Optional.ofNullable(overriddenIsClean);
    }

    public final ScmIdentity getIdentity() {
        return identity;
    }

    public Boolean isUnshallowRepoOnCI() {
        return unshallowRepoOnCI;
    }

    public Set<String> getReleaseBranchNames() {
        return releaseBranchNames;
    }

    public boolean isReleaseOnlyOnReleaseBranches() {
        return releaseOnlyOnReleaseBranches;
    }

    public boolean isIgnoreGlobalGitConfig() {
        return ignoreGlobalGitConfig;
    }

}
