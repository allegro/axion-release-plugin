package pl.allegro.tech.build.axion.release;

public final class TagPrefixConf {

    private TagPrefixConf() {
    }
    private static final String DEFAULT_PREFIX = "v";
    private static final String DEFAULT_SEPARATOR = "";

    public final static String DEFAULT_LEGACY_PREFIX = "release";
    public final static String DEFAULT_LEGACY_SEP = "-";

    public static String defaultPrefix() {
        return DEFAULT_PREFIX;
    }
    public static String defaultSeparator() {
        return DEFAULT_SEPARATOR;
    }

    public static String fullPrefix() {
        return DEFAULT_PREFIX + DEFAULT_SEPARATOR;
    }

    public static String fullLegacyPrefix() {
        return DEFAULT_LEGACY_PREFIX + DEFAULT_LEGACY_SEP;
    }
}
