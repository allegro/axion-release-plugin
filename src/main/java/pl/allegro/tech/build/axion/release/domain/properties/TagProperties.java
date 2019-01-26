package pl.allegro.tech.build.axion.release.domain.properties;

import groovy.lang.Closure;

public class TagProperties {

    private final String prefix;
    private final String versionSeparator;
    private final Closure<String> serialize;
    private final Closure<String> deserialize;
    private final Closure<String> initialVersion;

    public TagProperties(
        String prefix,
        String versionSeparator,
        Closure<String> serialize,
        Closure<String> deserialize,
        Closure<String> initialVersion
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

    public final Closure<String> getSerialize() {
        return serialize;
    }

    public final Closure<String> getDeserialize() {
        return deserialize;
    }

    public final Closure<String> getInitialVersion() {
        return initialVersion;
    }
}
