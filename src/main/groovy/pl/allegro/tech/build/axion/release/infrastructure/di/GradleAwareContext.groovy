package pl.allegro.tech.build.axion.release.infrastructure.di

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.infrastructure.config.LocalOnlyResolverFactory
import pl.allegro.tech.build.axion.release.infrastructure.config.RulesFactory
import pl.allegro.tech.build.axion.release.infrastructure.config.ScmPropertiesFactory

class GradleAwareContext {

    static Context create(Project project, VersionConfig versionConfig = config(project)) {
        ScmProperties scmProperties = ScmPropertiesFactory.create(project, versionConfig)
        ScmRepository scmRepository = ScmRepositoryFactory.create(scmProperties)

        return new Context(
                RulesFactory.create(project, versionConfig, scmRepository.currentPosition()),
                scmRepository,
                scmProperties,
                LocalOnlyResolverFactory.create(project, versionConfig)
        )
    }

    static VersionConfig config(Project project) {
        return project.extensions.getByType(VersionConfig)
    }

}
