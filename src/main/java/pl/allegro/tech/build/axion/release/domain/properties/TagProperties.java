package pl.allegro.tech.build.axion.release.domain.properties;

import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;

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
    private final String versionSeparator;
    private final Serializer serialize;
    private final Deserializer deserialize;
    private final InitialVersionSupplier initialVersion;

    public TagProperties(
        String prefix,
        String versionSeparator,
        Serializer serialize,
        Deserializer deserialize,
        InitialVersionSupplier initialVersion
    ) {
        this.prefix = prefix;
        this.versionSeparator = versionSeparator;
        this.serialize = serialize;
        this.deserialize = deserialize;
        this.initialVersion = initialVersion;
    }

    public final String getPrefix() {
        return prefix;
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
