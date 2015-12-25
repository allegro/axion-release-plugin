package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

enum NextVersionSerializer {

    DEFAULT('default',
    { NextVersionProperties rules, String version ->
        return rules.suffix ? version + rules.separator + rules.suffix : version
    },
    { NextVersionProperties rules, ScmPosition position ->
        if (rules.suffix.isEmpty()) {
            return position.latestTag
        }
        return position.latestTag.replaceFirst(rules.separator + rules.suffix, '')
    }
    )

    private final String type

    final Closure<String> serializer

    final Closure<String> deserializer

    private NextVersionSerializer(String type, Closure<String> serializer, Closure<String> deserializer) {
        this.type = type
        this.serializer = serializer
        this.deserializer = deserializer
    }
   
    static NextVersionSerializer find(String type) {
        NextVersionSerializer serializer = values().find { it.type == type }
        if (serializer == null) {
            throw new IllegalArgumentException("There is no predefined next version serializers with $type type. " +
                    "You can choose from: ${values().collect { it.type }}");
        }
        return serializer
    }
}
