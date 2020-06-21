package pl.allegro.tech.build.axion.release.domain.properties;

import com.github.zafarkhaja.semver.Version;
import groovy.lang.Closure;

public class VersionProperties {

    private final String forcedVersion;
    private final boolean forceSnapshot;
    private final boolean ignoreUncommittedChanges;
    private final Closure<String> versionCreator;
    private final Closure<Version> versionIncrementer;
    private final boolean sanitizeVersion;
    private final boolean useHighestVersion;
    private final boolean useGlobalVersion;
    private final MonorepoProperties monorepoProperties;

    public VersionProperties(
        String forcedVersion,
        boolean forceSnapshot,
        boolean ignoreUncommittedChanges,
        Closure<String> versionCreator,
        Closure<Version> versionIncrementer,
        boolean sanitizeVersion,
        boolean useHighestVersion,
        boolean useGlobalVersion,
        MonorepoProperties monorepoProperties
    ) {
        this.forcedVersion = forcedVersion;
        this.forceSnapshot = forceSnapshot;
        this.ignoreUncommittedChanges = ignoreUncommittedChanges;
        this.versionCreator = versionCreator;
        this.versionIncrementer = versionIncrementer;
        this.sanitizeVersion = sanitizeVersion;
        this.useHighestVersion = useHighestVersion;
        this.useGlobalVersion = useGlobalVersion;
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

    public final Closure<Version> getVersionIncrementer() {
        return versionIncrementer;
    }

    public final boolean isSanitizeVersion() {
        return sanitizeVersion;
    }

    public final boolean isUseHighestVersion() {
        return useHighestVersion;
    }

    public final boolean isUseGlobalVersion() { return useGlobalVersion; }

    public MonorepoProperties getMonorepoProperties() {
        return monorepoProperties;
    }
}
