package pl.allegro.tech.build.axion.release.infrastructure.config

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.domain.PredefinedVersionCreator
import pl.allegro.tech.build.axion.release.domain.PredefinedVersionIncrementer
import pl.allegro.tech.build.axion.release.domain.ProjectVersion
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties

import java.util.regex.Pattern

class VersionPropertiesFactory {

    private static final String DEPRECATED_FORCE_VERSION_PROPERTY = 'release.forceVersion'

    private static final String FORCE_VERSION_PROPERTY = 'release.version'

    private static final String IGNORE_UNCOMMITTED_CHANGES_PROPERTY = 'release.ignoreUncommittedChanges'

    private static final String FORCE_SNAPSHOT_PROPERTY = 'release.forceSnapshot'

    private static final String VERSION_INCREMENTER_PROPERTY = 'release.versionIncrementer'

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
                versionIncrementer: findVersionIncrementer(project, config, currentBranch),
                sanitizeVersion: config.sanitizeVersion
        )
    }

    private static Closure findVersionIncrementer(Project project, VersionConfig config, String currentBranch) {
        if(project.hasProperty(VERSION_INCREMENTER_PROPERTY)) {
            return PredefinedVersionIncrementer.versionIncrementerFor(project.property(VERSION_INCREMENTER_PROPERTY), [:])
        }

        return find(
                currentBranch,
                config.branchVersionIncrementer,
                config.versionIncrementer,
                { v ->
                    if(v instanceof List) {
                        return PredefinedVersionIncrementer.versionIncrementerFor(v[0], v[1])
                    }
                    return PredefinedVersionIncrementer.versionIncrementerFor(v, [:])
                }
        )
    }

    private static Closure findVersionCreator(VersionConfig config, String currentBranch) {
        return find(
                currentBranch,
                config.branchVersionCreator,
                config.versionCreator,
                { String s -> PredefinedVersionCreator.versionCreatorFor(s) }
        )
    }

    private
    static Closure find(String currentBranch, Map<String, Object> collection, Closure defaultValue, Closure<Closure> converter) {
        Object value = collection?.findResult { pattern, value ->
            Pattern.matches(pattern, currentBranch) ? value : null
        }

        if (value == null) {
            return defaultValue
        } else if (!(value instanceof Closure)) {
            return converter.call(value)
        } else {
            return value
        }
    }
}
