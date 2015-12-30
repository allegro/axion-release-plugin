package pl.allegro.tech.build.axion.release.domain

class VersionSanitizer {

    private static final String PATTERN = /[^A-Za-z0-9-._]/

    String sanitize(String version) {
        return version.replaceAll(PATTERN, '-')
    }
}
