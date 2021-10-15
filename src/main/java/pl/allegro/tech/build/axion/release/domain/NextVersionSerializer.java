package pl.allegro.tech.build.axion.release.domain;

import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;

import java.util.Arrays;
import java.util.stream.Collectors;

import static pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties.Deserializer;
import static pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties.Serializer;

public enum NextVersionSerializer {

    DEFAULT("default",
        (NextVersionProperties rules, String version) ->
            rules.getSuffix() != null ? version + rules.getSeparator() + rules.getSuffix() : version,
        (NextVersionProperties rules, ScmPosition position, String tag) ->
            rules.getSuffix().isEmpty() ? tag : tag.replaceFirst(rules.getSeparator() + rules.getSuffix(), "")
    );

    private final String type;

    public final Serializer serializer;

    public final Deserializer deserializer;

    NextVersionSerializer(String type, Serializer serializer, Deserializer deserializer) {
        this.type = type;
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    static NextVersionSerializer find(String type) {
        return Arrays.stream(values())
            .filter(it -> it.type.equals(type))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "There is no predefined next version serializers with " + type + " type. " +
                "You can choose from: " + Arrays.stream(values()).map(it -> it.type).collect(Collectors.joining())));
    }
}
