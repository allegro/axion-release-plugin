package pl.allegro.tech.build.axion.release.domain.properties;

import com.github.zafarkhaja.semver.Version;
import groovy.lang.Closure;

public class VersionProperties {

    private final String forcedVersion;
    private final boolean forceSnapshot;
    private final boolean ignoreUncommittedChanges;
    private final Closure<String> versionCreator;
    private final Closure<String> snapshotCreator;
    private final Closure<Version> versionIncrementer;
    private final boolean sanitizeVersion;
    private final boolean useHighestVersion;
    private final MonorepoProperties monorepoProperties;

    public VersionProperties(
        String forcedVersion,
        boolean forceSnapshot,
        boolean ignoreUncommittedChanges,
        Closure<String> versionCreator,
        Closure<String> snapshotCreator,
        Closure<Version> versionIncrementer,
        boolean sanitizeVersion,
        boolean useHighestVersion,
        MonorepoProperties monorepoProperties
    ) {
        this.forcedVersion = forcedVersion;
        this.forceSnapshot = forceSnapshot;
        this.ignoreUncommittedChanges = ignoreUncommittedChanges;
        this.versionCreator = versionCreator;
        this.snapshotCreator = snapshotCreator;
        this.versionIncrementer = versionIncrementer;
        this.sanitizeVersion = sanitizeVersion;
        this.useHighestVersion = useHighestVersion;
        this.monorepoProperties = monorepoProperties;
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

    public final Closure<String> getVersionCreator() {
        return versionCreator;
    }

    public final Closure<String> getSnapshotCreator() {
        return snapshotCreator;
    }

    public final Closure<Version> getVersionIncrementer() {
        return versionIncrementer;
    }

    public final boolean isSanitizeVersion() {
        return sanitizeVersion;
    }

    public final boolean isUseHighestVersion() {
        return useHighestVersion;
    }

    public MonorepoProperties getMonorepoProperties() {
        return monorepoProperties;
    }
}
