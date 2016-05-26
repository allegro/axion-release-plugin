package pl.allegro.tech.build.axion.release.domain.properties

import com.github.zafarkhaja.semver.Version;

import groovy.lang.Closure;
import pl.allegro.tech.build.axion.release.domain.PredefinedVersionCreator
import pl.allegro.tech.build.axion.release.domain.PredefinedVersionIncrementer

class VersionPropertiesBuilder {

    private String forcedVersion

    private Closure<Version> myVersionIncrementer = PredefinedVersionIncrementer.versionIncrementerFor('incrementPatch')

    private boolean forceSnapshot = false

    private boolean ignoreUncommittedChanges = true

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
                versionIncrementer: myVersionIncrementer,
                sanitizeVersion: true)
    }

    VersionPropertiesBuilder withVersionIncrementer(String incrementer) {
        this.myVersionIncrementer = PredefinedVersionIncrementer.versionIncrementerFor(incrementer)
        return this
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
}
