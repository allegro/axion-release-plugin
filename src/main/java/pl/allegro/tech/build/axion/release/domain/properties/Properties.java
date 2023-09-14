package pl.allegro.tech.build.axion.release.domain.properties;

import pl.allegro.tech.build.axion.release.domain.ChecksConfig;

public class Properties {

    private final boolean dryRun;
    private final VersionProperties version;
    private final TagProperties tag;
    private final ChecksConfig checks;
    private final NextVersionProperties nextVersion;
    private final HooksProperties hooks;

    public Properties(
        boolean dryRun,
        VersionProperties version,
        TagProperties tag,
        ChecksConfig checks,
        NextVersionProperties nextVersion,
        HooksProperties hooks
    ) {
        this.dryRun = dryRun;
        this.version = version;
        this.tag = tag;
        this.checks = checks;
        this.nextVersion = nextVersion;
        this.hooks = hooks;
    }

    public final boolean isDryRun() {
        return dryRun;
    }

    public final VersionProperties getVersion() {
        return version;
    }

    public final TagProperties getTag() {
        return tag;
    }

    public final ChecksConfig getChecks() {
        return checks;
    }

    public final NextVersionProperties getNextVersion() {
        return nextVersion;
    }

    public final HooksProperties getHooks() {
        return hooks;
    }
}
