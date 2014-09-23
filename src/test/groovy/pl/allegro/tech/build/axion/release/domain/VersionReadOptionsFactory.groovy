package pl.allegro.tech.build.axion.release.domain

class VersionReadOptionsFactory {

    static VersionReadOptions empty() {
        return new VersionReadOptions(false, null)
    }

    static VersionReadOptions withForcedVersion(String version) {
       return new VersionReadOptions(true, version)
    }

}
