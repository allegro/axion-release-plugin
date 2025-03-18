package pl.allegro.tech.build.axion.release.domain.properties;

import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class TagProperties {

    public interface Serializer {
        String apply(TagProperties tagProperties, String version);
    }

    public interface Deserializer {
        String apply(TagProperties tagProperties, ScmPosition position, String tag);
    }

    public interface InitialVersionSupplier {
        String apply(TagProperties tagProperties, ScmPosition position);
    }

    private final String prefix;
    private final List<String> fallbackPrefixes;
    private final String versionSeparator;
    private final Serializer serialize;
    private final Deserializer deserialize;
    private final InitialVersionSupplier initialVersion;

    public TagProperties(
        String prefix,
        List<String> fallbackPrefixes,
        String versionSeparator,
        Serializer serialize,
        Deserializer deserialize,
        InitialVersionSupplier initialVersion
    ) {
        this.prefix = prefix;
        this.fallbackPrefixes = fallbackPrefixes;
        this.versionSeparator = versionSeparator;
        this.serialize = serialize;
        this.deserialize = deserialize;
        this.initialVersion = initialVersion;
    }

    public final String getPrefix() {
        return prefix;
    }

    public final List<String> getFallbackPrefixes() {
        return fallbackPrefixes;
    }

    public final List<String> getAllPrefixes() {
        return Stream.concat(
            Stream.of(prefix), // main prefix takes precedence
            fallbackPrefixes.stream()
        ).collect(toList());
    }

    public final String getVersionSeparator() {
        return versionSeparator;
    }

    public final Serializer getSerialize() {
        return serialize;
    }

    public final Deserializer getDeserialize() {
        return deserialize;
    }

    public final InitialVersionSupplier getInitialVersion() {
        return initialVersion;
    }
}
