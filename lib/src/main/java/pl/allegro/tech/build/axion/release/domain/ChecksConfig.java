package pl.allegro.tech.build.axion.release.domain;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;

import javax.inject.Inject;

@SuppressWarnings("UnstableApiUsage")
public abstract class ChecksConfig extends BaseExtension {

    private static final String DISABLE_UNCOMMITTED_CHANGES_CHECK = "release.disableUncommittedCheck";
    private static final String DISABLE_AHEAD_OF_REMOTE_CHECK = "release.disableRemoteCheck";
    private static final String DISABLE_SNAPSHOT_DEPENDENCIES_CHECK = "release.disableSnapshotsCheck";
    private static final String DISABLE_CHECKS = "release.disableChecks";

    @Input
    public abstract Property<Boolean> getAheadOfRemote();

    @Input
    public abstract Property<Boolean> getUncommittedChanges();

    @Input
    public abstract Property<Boolean> getSnapshotDependencies();

    @Inject
    public ChecksConfig() {
        getAheadOfRemote().convention(true);
        getUncommittedChanges().convention(true);
        getSnapshotDependencies().convention(true);
    }

    public Provider<Boolean> checkUncommittedChanges() {
        return enabled(DISABLE_UNCOMMITTED_CHANGES_CHECK)
            .orElse(getUncommittedChanges());
    }

    public Provider<Boolean> checkAheadOfRemote() {
        return enabled(DISABLE_AHEAD_OF_REMOTE_CHECK)
            .orElse(getAheadOfRemote());
    }

    public Provider<Boolean> checkSnapshotDependencies() {
        return enabled(DISABLE_SNAPSHOT_DEPENDENCIES_CHECK)
            .orElse(getSnapshotDependencies());
    }

    private Provider<Boolean> enabled(String property) {
        // if either property is present this feature isn't enabled
        return gradlePropertyPresent(DISABLE_CHECKS)
            .orElse(gradlePropertyPresent(property))
            .map(it -> false);
    }

}
