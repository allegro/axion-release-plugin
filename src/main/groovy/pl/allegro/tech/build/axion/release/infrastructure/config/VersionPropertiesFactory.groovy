package pl.allegro.tech.build.axion.release.infrastructure.config

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.domain.PredefinedVersionCreator
import pl.allegro.tech.build.axion.release.domain.PredefinedVersionIncrementer
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.VersionIncrementerContext
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties

import java.util.regex.Pattern

class VersionPropertiesFactory {

    private static final String DEPRECATED_FORCE_VERSION_PROPERTY = 'release.forceVersion'

    private static final String FORCE_VERSION_PROPERTY = 'release.version'

    private static final String IGNORE_UNCOMMITTED_CHANGES_PROPERTY = 'release.ignoreUncommittedChanges'

    private static final String FORCE_SNAPSHOT_PROPERTY = 'release.forceSnapshot'

    private static final String USE_HIGHEST_VERSION_PROPERTY = 'release.useHighestVersion'

    private static final String VERSION_INCREMENTER_PROPERTY = 'release.versionIncrementer'

    private static final String VERSION_CREATOR_PROPERTY = 'release.versionCreator'

    static VersionProperties create(Project project, VersionConfig config, String currentBranch) {
        String forceVersionValue = project.hasProperty(FORCE_VERSION_PROPERTY) ? project.property(FORCE_VERSION_PROPERTY) : null
        if (forceVersionValue == null) {
            forceVersionValue = project.hasProperty(DEPRECATED_FORCE_VERSION_PROPERTY) ? project.property(DEPRECATED_FORCE_VERSION_PROPERTY) : null
        }

        boolean ignoreUncommittedChanges = project.hasProperty(IGNORE_UNCOMMITTED_CHANGES_PROPERTY) ?: config.ignoreUncommittedChanges
        boolean forceSnapshot = project.hasProperty(FORCE_SNAPSHOT_PROPERTY)

        boolean useHighestVersion = project.hasProperty(USE_HIGHEST_VERSION_PROPERTY) ?: config.useHighestVersion

        return new VersionProperties(
            forceVersionValue?.trim() ? forceVersionValue.trim() : null,
            forceSnapshot,
            ignoreUncommittedChanges,
            findVersionCreator(project, config, currentBranch),
            config.snapshotCreator,
            findVersionIncrementer(project, config, currentBranch),
            config.sanitizeVersion,
            useHighestVersion,
            MonorepoPropertiesFactory.create(project, config.monorepoConfig, currentBranch)
        )
    }

    private static VersionProperties.Incrementer findVersionIncrementer(Project project, VersionConfig config, String currentBranch) {
        if (project.hasProperty(VERSION_INCREMENTER_PROPERTY)) {
            return PredefinedVersionIncrementer.versionIncrementerFor(project.property(VERSION_INCREMENTER_PROPERTY), [:])
        }

        return findVersionIncrementer(
            currentBranch,
            config.branchVersionIncrementer,
            { VersionIncrementerContext a -> config.versionIncrementer.apply(a) },
            { v ->
                if (v instanceof List) {
                    return PredefinedVersionIncrementer.versionIncrementerFor(v[0], v[1])
                }
                return PredefinedVersionIncrementer.versionIncrementerFor(v, [:])
            }
        )
    }

    private static VersionProperties.Creator findVersionCreator(Project project, VersionConfig config, String currentBranch) {
        if (project.hasProperty(VERSION_CREATOR_PROPERTY)) {
            return PredefinedVersionCreator.versionCreatorFor(project.property(VERSION_CREATOR_PROPERTY))
        }

        return find(
            currentBranch,
            config.branchVersionCreator,
            config.versionCreator,
            { String s -> PredefinedVersionCreator.versionCreatorFor(s) }
        )
    }

    private
    static VersionProperties.Incrementer findVersionIncrementer(String currentBranch,
                                                                                  Map<String, Object> collection,
                                                                                  VersionProperties.Incrementer defaultValue,
                                                                                  Closure<VersionProperties.Incrementer> converter) {
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

    private
    static VersionProperties.Creator find(String currentBranch, Map<String, Object> collection, VersionProperties.Creator defaultValue, Closure<VersionProperties.Creator> converter) {
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
