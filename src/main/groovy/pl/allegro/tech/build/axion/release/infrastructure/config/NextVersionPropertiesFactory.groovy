package pl.allegro.tech.build.axion.release.infrastructure.config

import com.github.zafarkhaja.semver.Version
import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.domain.PredefinedVersionIncrementer
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties

class NextVersionPropertiesFactory {

    private static final String NEXT_VERSION_PROPERTY = "release.version"

    private static final String NEXT_VERSION_INCREMENTER_PROPERTY = "release.incrementer"

    private static final String DEPRECATED_NEXT_VERSION_PROPERTY = "release.nextVersion"

    static NextVersionProperties create(Project project, VersionConfig versionConfig) {
        String nextVersion = project.hasProperty(NEXT_VERSION_PROPERTY) ? project.property(NEXT_VERSION_PROPERTY) : null
        String versionIncrementerName = project.hasProperty(NEXT_VERSION_INCREMENTER_PROPERTY) ? project.property(NEXT_VERSION_INCREMENTER_PROPERTY) : null
        Closure<Version> versionIncrementer = versionIncrementerName ?
            PredefinedVersionIncrementer.versionIncrementerFor(versionIncrementerName) :
            versionConfig.versionIncrementer

        if (nextVersion == null && project.hasProperty(DEPRECATED_NEXT_VERSION_PROPERTY)) {
            project.logger.warn("Using deprecated parameter: $DEPRECATED_NEXT_VERSION_PROPERTY! Use $NEXT_VERSION_PROPERTY instead.")
            nextVersion = project.property(DEPRECATED_NEXT_VERSION_PROPERTY)
        }

        return new NextVersionProperties(nextVersion: nextVersion,
            versionIncrementer: versionIncrementer,
            suffix: versionConfig.nextVersion.suffix,
            separator: versionConfig.nextVersion.separator,
            serializer: versionConfig.nextVersion.serializer,
            deserializer: versionConfig.nextVersion.deserializer)
    }

}
