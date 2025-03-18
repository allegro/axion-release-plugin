package pl.allegro.tech.build.axion.release.domain.properties;

import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;

public class NextVersionProperties {

    @FunctionalInterface
    public interface Serializer {
        String apply(NextVersionProperties nextVersionProperties, String version);
    }

    @FunctionalInterface
    public interface Deserializer {
        String apply(NextVersionProperties nextVersionProperties, ScmPosition position, String tag);
    }

    private final String nextVersion;
    private final String suffix;
    private final String separator;
    private final String versionIncrementer;
    private final Serializer serializer;
    private final Deserializer deserializer;

    public NextVersionProperties(
        String nextVersion,
        String suffix,
        String separator,
        String versionIncrementer,
        Serializer serializer,
        Deserializer deserializer
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

    public final Serializer getSerializer() {
        return serializer;
    }

    public final Deserializer getDeserializer() {
        return deserializer;
    }
}
