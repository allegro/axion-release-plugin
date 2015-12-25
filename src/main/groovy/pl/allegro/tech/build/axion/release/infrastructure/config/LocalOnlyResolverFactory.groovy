package pl.allegro.tech.build.axion.release.infrastructure.config

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver
import pl.allegro.tech.build.axion.release.domain.VersionConfig

class LocalOnlyResolverFactory {

    private static final String LOCAL_ONLY = "release.localOnly"

    static LocalOnlyResolver create(Project project, VersionConfig config) {
        boolean baseValue = project.hasProperty(LOCAL_ONLY) || config.localOnly
        return new LocalOnlyResolver(baseValue)
    }

}
