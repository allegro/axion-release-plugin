package pl.allegro.tech.build.axion.release.domain.properties;

import com.github.zafarkhaja.semver.Version;
import pl.allegro.tech.build.axion.release.domain.MonorepoConfig;
import pl.allegro.tech.build.axion.release.domain.VersionIncrementerContext;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;

public class VersionProperties {

    public interface Creator {
        String apply(String versionFromTag, ScmPosition position);
    }

    public interface Incrementer {
        Version apply(VersionIncrementerContext versionIncrementerContext);
    }

    private final String forcedVersion;
    private final boolean forceSnapshot;
    private final boolean ignoreUncommittedChanges;
    private final Creator versionCreator;
    private final Creator snapshotCreator;
    private final Incrementer versionIncrementer;
    private final boolean sanitizeVersion;
    private final boolean useHighestVersion;
    private final MonorepoConfig monorepoConfig;

    public VersionProperties(
        String forcedVersion,
        boolean forceSnapshot,
        boolean ignoreUncommittedChanges,
        Creator versionCreator,
        Creator snapshotCreator,
        Incrementer versionIncrementer,
        boolean sanitizeVersion,
        boolean useHighestVersion,
        MonorepoConfig monorepoConfig
    ) {
        this.forcedVersion = forcedVersion;
        this.forceSnapshot = forceSnapshot;
        this.ignoreUncommittedChanges = ignoreUncommittedChanges;
        this.versionCreator = versionCreator;
        this.snapshotCreator = snapshotCreator;
        this.versionIncrementer = versionIncrementer;
        this.sanitizeVersion = sanitizeVersion;
        this.useHighestVersion = useHighestVersion;
        this.monorepoConfig = monorepoConfig;
    }

    public boolean forceVersion() {
        return forcedVersion != null;
    }

    public final String getForcedVersion() {
        return forcedVersion;
    }

    public final boolean isForceSnapshot() {
        return forceSnapshot;
    }

    public final boolean isIgnoreUncommittedChanges() {
        return ignoreUncommittedChanges;
    }

    public final Creator getVersionCreator() {
        return versionCreator;
    }

    public final Creator getSnapshotCreator() {
        return snapshotCreator;
    }

    public final Incrementer getVersionIncrementer() {
        return versionIncrementer;
    }

    public final boolean isSanitizeVersion() {
        return sanitizeVersion;
    }

    public final boolean isUseHighestVersion() {
        return useHighestVersion;
    }

    public MonorepoConfig getMonorepoConfig() {
        return monorepoConfig;
    }
}
