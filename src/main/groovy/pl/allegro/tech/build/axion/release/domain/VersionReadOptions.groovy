package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project

class VersionReadOptions {

    private static final String DEPRECATED_FORCE_VERSION_PROPERTY = 'release.forceVersion'

    private static final String FORCE_VERSION_PROPERTY = 'release.version'

    private static final String IGNORE_UNCOMMITTED_CHANGES_PROPERTY = 'release.ignoreUncommittedChanges'

    private static final String FORCE_SNAPSHOT_PROPERTY = 'release.forceSnapshot'
    
    final boolean forceVersion

    final String forcedVersion
    
    final boolean ignoreUncommittedChanges

    final boolean forceSnapshot

    VersionReadOptions(String forcedVersion, ignoreUncommittedChanges, forceSnapshot) {
        this.forceVersion = forcedVersion != null
        this.forcedVersion = forcedVersion
        this.ignoreUncommittedChanges = ignoreUncommittedChanges
        this.forceSnapshot = forceSnapshot
    }

    static VersionReadOptions fromProject(Project project, VersionConfig config) {
        String forceVersionValue = project.hasProperty(FORCE_VERSION_PROPERTY) ? project.property(FORCE_VERSION_PROPERTY) : null
        if(forceVersionValue == null) {
            forceVersionValue = project.hasProperty(DEPRECATED_FORCE_VERSION_PROPERTY) ? project.property(DEPRECATED_FORCE_VERSION_PROPERTY) : null
        }

        boolean ignoreUncommittedChanges = project.hasProperty(IGNORE_UNCOMMITTED_CHANGES_PROPERTY) ?: config.ignoreUncommittedChanges
        boolean forceSnapshot = project.hasProperty(FORCE_SNAPSHOT_PROPERTY)
        return new VersionReadOptions(forceVersionValue?.trim() ? forceVersionValue.trim() : null, ignoreUncommittedChanges, forceSnapshot)
    }

    static VersionReadOptions defaultOptions() {
        return new VersionReadOptions(null, true, false)
    }
}
