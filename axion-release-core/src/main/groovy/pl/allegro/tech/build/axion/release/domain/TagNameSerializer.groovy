package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

enum TagNameSerializer {

    DEFAULT('default',
            { TagProperties rules, String version ->
                return rules.prefix ? rules.prefix + rules.versionSeparator + version : version
            },
            { TagProperties rules, ScmPosition position, String tagName ->
                if (rules.prefix.isEmpty()) {
                    return tagName
                }
                return tagName.substring(rules.prefix.length() + rules.versionSeparator.length())
            }
    )

    private final String type

    final Closure<String> serializer

    final Closure<String> deserializer

    private TagNameSerializer(String type, Closure serializer, Closure deserializer) {
        this.type = type
        this.serializer = serializer
        this.deserializer = deserializer
    }
}
