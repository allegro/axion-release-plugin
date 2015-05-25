package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project

class VersionReadOptions {

    private static final String FORCE_VERSION_PROPERTY = 'release.forceVersion'

    final boolean forceVersion

    final String forcedVersion

    VersionReadOptions(String forcedVersion) {
        this.forceVersion = forcedVersion != null
        this.forcedVersion = forcedVersion
    }

    static VersionReadOptions fromProject(Project project) {
        String forceVersionValue = project.hasProperty(FORCE_VERSION_PROPERTY) ? project.property(FORCE_VERSION_PROPERTY) : null
        return new VersionReadOptions(forceVersionValue?.trim() ? forceVersionValue.trim() : null)
    }

    static VersionReadOptions defaultOptions() {
        return new VersionReadOptions(null)
    }
}
