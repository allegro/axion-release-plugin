package pl.allegro.tech.build.axion.release.domain.properties;

import groovy.lang.Closure;

public class NextVersionProperties {

    private final String nextVersion;
    private final String suffix;
    private final String separator;
    private final String versionIncrementer;
    private final Closure<String> serializer;
    private final Closure<String> deserializer;

    public NextVersionProperties(
        String nextVersion,
        String suffix,
        String separator,
        String versionIncrementer,
        Closure<String> serializer,
        Closure<String> deserializer
    ) {
        this.nextVersion = nextVersion;
        this.suffix = suffix;
        this.separator = separator;
        this.versionIncrementer = versionIncrementer;
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    public final String getNextVersion() {
        return nextVersion;
    }

    public final String getSuffix() {
        return suffix;
    }

    public final String getSeparator() {
        return separator;
    }

    public final String getVersionIncrementer() {
        return versionIncrementer;
    }

    public final Closure<String> getSerializer() {
        return serializer;
    }

    public final Closure<String> getDeserializer() {
        return deserializer;
    }
}
