package pl.allegro.tech.build.axion.release.infrastructure.config

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.domain.NextVersionConfig
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties

class NextVersionPropertiesFactory {

    private static final String NEXT_VERSION_PROPERTY = "release.version"

    private static final String NEXT_VERSION_INCREMENTER_PROPERTY = "release.incrementer"

    private static final String DEPRECATED_NEXT_VERSION_PROPERTY = "release.nextVersion"

    static NextVersionProperties create(Project project, NextVersionConfig config) {
        if (config.suffix == null || config.suffix.isEmpty()) {
            String message = "scmVersion.nextVersion.suffix can't be empty! Empty suffix will prevent axion-release from distinguishing nextVersion from regular versions";
            project.logger.error(message)
            throw new IllegalArgumentException(message)
        }

        String nextVersion = project.hasProperty(NEXT_VERSION_PROPERTY) ? project.property(NEXT_VERSION_PROPERTY) : null
        String versionIncrementerName = project.hasProperty(NEXT_VERSION_INCREMENTER_PROPERTY) ? project.property(NEXT_VERSION_INCREMENTER_PROPERTY) : null

        if (nextVersion == null && project.hasProperty(DEPRECATED_NEXT_VERSION_PROPERTY)) {
            project.logger.warn("Using deprecated parameter: $DEPRECATED_NEXT_VERSION_PROPERTY! Use $NEXT_VERSION_PROPERTY instead.")
            nextVersion = project.property(DEPRECATED_NEXT_VERSION_PROPERTY)
        }

        return new NextVersionProperties(
            nextVersion,
            config.suffix,
            config.separator,
            versionIncrementerName,
            config.serializer,
            config.deserializer)
    }

}
