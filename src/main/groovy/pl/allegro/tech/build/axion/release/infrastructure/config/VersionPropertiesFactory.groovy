package pl.allegro.tech.build.axion.release.infrastructure.config

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties

import java.util.regex.Pattern

class VersionPropertiesFactory {

    private static final String DEPRECATED_FORCE_VERSION_PROPERTY = 'release.forceVersion'

    private static final String FORCE_VERSION_PROPERTY = 'release.version'

    private static final String IGNORE_UNCOMMITTED_CHANGES_PROPERTY = 'release.ignoreUncommittedChanges'

    private static final String FORCE_SNAPSHOT_PROPERTY = 'release.forceSnapshot'

    static VersionProperties create(Project project, VersionConfig config, String currentBranch) {
        String forceVersionValue = project.hasProperty(FORCE_VERSION_PROPERTY) ? project.property(FORCE_VERSION_PROPERTY) : null
        if (forceVersionValue == null) {
            forceVersionValue = project.hasProperty(DEPRECATED_FORCE_VERSION_PROPERTY) ? project.property(DEPRECATED_FORCE_VERSION_PROPERTY) : null
        }

        boolean ignoreUncommittedChanges = project.hasProperty(IGNORE_UNCOMMITTED_CHANGES_PROPERTY) ?: config.ignoreUncommittedChanges
        boolean forceSnapshot = project.hasProperty(FORCE_SNAPSHOT_PROPERTY)

        return new VersionProperties(
                forcedVersion: forceVersionValue?.trim() ? forceVersionValue.trim() : null,
                forceSnapshot: forceSnapshot,
                ignoreUncommittedChanges: ignoreUncommittedChanges,
                versionCreator: findVersionCreator(config, currentBranch),
                versionIncrementer: config.versionIncrementer,
                sanitizeVersion: config.sanitizeVersion
        )
    }

    private static Closure findVersionCreator(VersionConfig config, String currentBranch) {
        Closure versionCreator = config.branchVersionCreators?.findResult { pattern, creator ->
            Pattern.matches(pattern, currentBranch) ? creator : null
        }

        return versionCreator ?: config.versionCreator
    }
}
