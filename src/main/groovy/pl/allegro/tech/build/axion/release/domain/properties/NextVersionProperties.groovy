package pl.allegro.tech.build.axion.release.domain.properties

import com.github.zafarkhaja.semver.Version
import groovy.transform.Immutable

@Immutable
class NextVersionProperties {

    final String nextVersion

    final String suffix

    final String separator

    final Closure<Version> versionIncrementer

    final Closure<String> serializer

    final Closure<String> deserializer

}
