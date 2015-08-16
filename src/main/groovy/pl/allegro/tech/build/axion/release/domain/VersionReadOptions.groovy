package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project

class VersionReadOptions {

    private static final String FORCE_VERSION_PROPERTY = 'release.forceVersion'

    private static final String IGNORE_UNCOMMITTED_CHANGES_PROPERTY = 'release.ignoreUncommittedChanges'
    
    final boolean forceVersion

    final String forcedVersion
    
    final boolean ignoreUncommittedChanges

    VersionReadOptions(String forcedVersion, ignoreUncommittedChanges) {
        this.forceVersion = forcedVersion != null
        this.forcedVersion = forcedVersion
        this.ignoreUncommittedChanges = ignoreUncommittedChanges
    }

    static VersionReadOptions fromProject(Project project, VersionConfig config) {
        String forceVersionValue = project.hasProperty(FORCE_VERSION_PROPERTY) ? project.property(FORCE_VERSION_PROPERTY) : null
        boolean ignoreUncommittedChanges = project.hasProperty(IGNORE_UNCOMMITTED_CHANGES_PROPERTY) ?: config.ignoreUncommittedChanges
        return new VersionReadOptions(forceVersionValue?.trim() ? forceVersionValue.trim() : null, ignoreUncommittedChanges)
    }

    static VersionReadOptions defaultOptions() {
        return new VersionReadOptions(null, true)
    }
}
