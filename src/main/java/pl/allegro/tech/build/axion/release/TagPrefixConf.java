package pl.allegro.tech.build.axion.release;

public final class TagPrefixConf {

    private TagPrefixConf() {
    }
    private static String defaultPrefix = "v";
    private static String defaultSeparator = "";

    public static String prefix() {
        return defaultPrefix;
    }
    public static void setDefPrefix(String prefix) {
        defaultPrefix = prefix;
    }
    public static void setDefSeparator(String sep) {
        defaultSeparator = sep;
    }

    public static String separator() {
        return defaultSeparator;
    }

    public static String fullPrefix() {
        return defaultPrefix + defaultSeparator;
    }
}
