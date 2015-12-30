package pl.allegro.tech.build.axion.release.domain.properties

class Properties {

    final boolean dryRun

    final VersionProperties version

    final TagProperties tag

    final ChecksProperties checks

    final NextVersionProperties nextVersion

    final HooksProperties hooks

    Properties(boolean dryRun, VersionProperties version, TagProperties tag, ChecksProperties checks,
               NextVersionProperties nextVersion, HooksProperties hooks) {
        this.dryRun = dryRun
        this.version = version
        this.tag = tag
        this.checks = checks
        this.nextVersion = nextVersion
        this.hooks = hooks
    }
}
