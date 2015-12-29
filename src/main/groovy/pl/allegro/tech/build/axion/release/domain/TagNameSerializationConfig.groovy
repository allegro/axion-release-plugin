package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

class TagNameSerializationConfig {

    static final String DEFAULT_VERSION_SEPARATOR = '-'

    static final String DEFAULT_PREFIX = 'release'

    String prefix = DEFAULT_PREFIX

    Map<String, String> branchPrefix = [:]

    String versionSeparator = DEFAULT_VERSION_SEPARATOR

    Closure serialize = TagNameSerializer.DEFAULT.serializer

    Closure deserialize = TagNameSerializer.DEFAULT.deserializer

    Closure initialVersion = defaultInitialVersion()

    private static Closure defaultInitialVersion() {
        return { TagProperties rules, ScmPosition position ->
            return '0.1.0'
        }
    }
}
