package pl.allegro.tech.build.axion.release.domain.properties

import pl.allegro.tech.build.axion.release.Fixtures
import pl.allegro.tech.build.axion.release.domain.MonorepoConfig
import pl.allegro.tech.build.axion.release.domain.PredefinedSnapshotCreator
import pl.allegro.tech.build.axion.release.domain.PredefinedVersionCreator
import pl.allegro.tech.build.axion.release.domain.PredefinedVersionIncrementer

class VersionPropertiesBuilder {

    private String forcedVersion

    private boolean forceSnapshot = false

    private boolean ignoreUncommittedChanges = true

    private boolean useHighestVersion = false

    private MonorepoConfig monorepoConfig = Fixtures.monorepoConfig()

    private VersionProperties.Creator versionCreator = PredefinedVersionCreator.SIMPLE.versionCreator

    private VersionProperties.Creator snapshotCreator = PredefinedSnapshotCreator.SIMPLE.snapshotCreator

    private boolean sanitizeVersion = true

    private VersionPropertiesBuilder() {
    }

    static VersionPropertiesBuilder versionProperties() {
        return new VersionPropertiesBuilder()
    }

    VersionProperties build() {
        return new VersionProperties(
            forcedVersion,
            forceSnapshot,
            ignoreUncommittedChanges,
            versionCreator,
            snapshotCreator,
            PredefinedVersionIncrementer.versionIncrementerFor('incrementPatch'),
            sanitizeVersion,
            useHighestVersion,
            monorepoConfig
        )
    }

    VersionPropertiesBuilder forceVersion(String version) {
        this.forcedVersion = version
        return this
    }

    VersionPropertiesBuilder forceSnapshot() {
        this.forceSnapshot = true
        return this
    }

    VersionPropertiesBuilder dontIgnoreUncommittedChanges() {
        this.ignoreUncommittedChanges = false
        return this
    }

    VersionPropertiesBuilder useHighestVersion() {
        this.useHighestVersion = true
        return this
    }

    VersionPropertiesBuilder supportMonorepos(MonorepoConfig monorepoConfig) {
        this.monorepoConfig = monorepoConfig
        return this
    }

    VersionPropertiesBuilder withVersionCreator(Closure<String> creator) {
        this.versionCreator = creator
        return this
    }

    VersionPropertiesBuilder withSnapshotCreator(Closure<String> creator) {
        this.snapshotCreator = creator
        return this
    }

    VersionPropertiesBuilder dontSanitizeVersion() {
        this.sanitizeVersion = false
        return this
    }
}
