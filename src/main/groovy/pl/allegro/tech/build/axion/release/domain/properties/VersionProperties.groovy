package pl.allegro.tech.build.axion.release.domain.properties

import com.github.zafarkhaja.semver.Version
import groovy.transform.Immutable

@Immutable
class VersionProperties {

    final String forcedVersion

    final boolean forceSnapshot

    final boolean ignoreUncommittedChanges

    final Closure<String> versionCreator

    final Closure<Version> versionIncrementer

    final boolean sanitizeVersion

    boolean forceVersion() {
        return forcedVersion != null
    }
}
