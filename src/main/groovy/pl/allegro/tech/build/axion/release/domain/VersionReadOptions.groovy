package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project

class VersionReadOptions {

    private static final String FORCE_VERSION_PROPERTY = 'release.forceVersion'

    final boolean forceVersion

    final String forcedVersion

    VersionReadOptions(boolean forceVersion, String forcedVersion) {
        this.forceVersion = forceVersion
        this.forcedVersion = forcedVersion
    }

    static VersionReadOptions fromProject(Project project) {
        String forceVersionValue = project.hasProperty(FORCE_VERSION_PROPERTY) ? project.property(FORCE_VERSION_PROPERTY) : null
        boolean forceVersion = forceVersionValue != null && !forceVersionValue.trim().isEmpty()

        return new VersionReadOptions(forceVersion, forceVersionValue)
    }

    static VersionReadOptions defaultOptions() {
        return new VersionReadOptions(false, false, null, false, null)
    }
}
