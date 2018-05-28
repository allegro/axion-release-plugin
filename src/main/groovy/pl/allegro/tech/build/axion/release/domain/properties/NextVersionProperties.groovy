package pl.allegro.tech.build.axion.release.domain.properties

import groovy.transform.Immutable

@Immutable
class NextVersionProperties {

    final String nextVersion

    final String suffix

    final String separator

    final String versionIncrementer

    final Closure<String> serializer

    final Closure<String> deserializer

}
