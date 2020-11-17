package pl.allegro.tech.build.axion.release.domain.properties;

import com.github.zafarkhaja.semver.Version;
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
    private final Incrementer versionIncrementer;
    private final boolean sanitizeVersion;
    private final boolean useHighestVersion;
    private final MonorepoProperties monorepoProperties;

    public VersionProperties(
        String forcedVersion,
        boolean forceSnapshot,
        boolean ignoreUncommittedChanges,
        Creator versionCreator,
        Incrementer versionIncrementer,
        boolean sanitizeVersion,
        boolean useHighestVersion,
        MonorepoProperties monorepoProperties
    ) {
        this.forcedVersion = forcedVersion;
        this.forceSnapshot = forceSnapshot;
        this.ignoreUncommittedChanges = ignoreUncommittedChanges;
        this.versionCreator = versionCreator;
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

    public final Creator getVersionCreator() {
        return versionCreator;
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

    public MonorepoProperties getMonorepoProperties() {
        return monorepoProperties;
    }
}
