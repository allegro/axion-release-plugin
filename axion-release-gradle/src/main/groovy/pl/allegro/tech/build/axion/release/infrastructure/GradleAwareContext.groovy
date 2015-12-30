package pl.allegro.tech.build.axion.release.infrastructure

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.config.VersionConfig
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.factory.LocalOnlyResolverFactory
import pl.allegro.tech.build.axion.release.factory.RulesFactory
import pl.allegro.tech.build.axion.release.factory.ScmPropertiesFactory
import pl.allegro.tech.build.axion.release.infrastructure.di.Context
import pl.allegro.tech.build.axion.release.infrastructure.di.ScmRepositoryFactory

class GradleAwareContext {

    static Context create(Project project) {
        VersionConfig config = config(project)

        ScmProperties scmProperties = ScmPropertiesFactory.create(project, config)
        ScmRepository scmRepository = ScmRepositoryFactory.create(scmProperties)

        return new Context(
                RulesFactory.create(project, config, scmRepository.currentBranch()),
                scmRepository,
                scmProperties,
                LocalOnlyResolverFactory.create(project, config)
        )
    }

    static VersionConfig config(Project project) {
        return project.extensions.getByType(VersionConfig)
    }

}
