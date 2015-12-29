package pl.allegro.tech.build.axion.release.domain.properties

import groovy.transform.Immutable

@Immutable
class TagProperties {

    final String prefix

    final String versionSeparator

    final Closure<String> serialize

    final Closure<String> deserialize

    final Closure<String> initialVersion

}
