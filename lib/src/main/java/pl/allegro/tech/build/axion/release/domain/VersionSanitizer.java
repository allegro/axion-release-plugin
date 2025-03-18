package pl.allegro.tech.build.axion.release.domain;

public class VersionSanitizer {

    private static final String PATTERN = "[^A-Za-z0-9-._]";

    public String sanitize(String version) {
        return version.replaceAll(PATTERN, "-");
    }
}
