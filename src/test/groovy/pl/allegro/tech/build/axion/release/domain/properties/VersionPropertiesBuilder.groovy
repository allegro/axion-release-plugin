package pl.allegro.tech.build.axion.release.domain.properties

import pl.allegro.tech.build.axion.release.domain.PredefinedVersionCreator
import pl.allegro.tech.build.axion.release.domain.PredefinedVersionIncrementer

class VersionPropertiesBuilder {

    private String forcedVersion

    private boolean forceSnapshot = false

    private boolean ignoreUncommittedChanges = true
		
		private boolean useHighestVersion = false

    private VersionPropertiesBuilder() {
    }

    static VersionPropertiesBuilder versionProperties() {
        return new VersionPropertiesBuilder()
    }

    VersionProperties build() {
        return new VersionProperties(
                forcedVersion: forcedVersion,
                forceSnapshot: forceSnapshot,
                ignoreUncommittedChanges: ignoreUncommittedChanges,
                versionCreator: PredefinedVersionCreator.SIMPLE.versionCreator,
                versionIncrementer: PredefinedVersionIncrementer.versionIncrementerFor('incrementPatch'),
                sanitizeVersion: true,
								useHighestVersion: useHighestVersion)
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
}
