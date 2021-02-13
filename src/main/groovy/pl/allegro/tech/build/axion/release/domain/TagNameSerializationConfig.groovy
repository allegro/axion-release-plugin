package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

class TagNameSerializationConfig {

    static final String DEFAULT_VERSION_SEPARATOR = '-'

    static final String DEFAULT_PREFIX = 'release'

    @Input
    String prefix = DEFAULT_PREFIX

    @Input
    Map<String, String> branchPrefix = [:]

    @Input
    String versionSeparator = DEFAULT_VERSION_SEPARATOR

    @Nested
    Closure serialize = TagNameSerializer.DEFAULT.serializer

    @Nested
    Closure deserialize = TagNameSerializer.DEFAULT.deserializer

    @Nested
    Closure initialVersion = defaultInitialVersion()

    private static Closure defaultInitialVersion() {
        return { TagProperties rules, ScmPosition position ->
            return '0.1.0'
        }
    }
}
