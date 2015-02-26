package pl.allegro.tech.build.axion.release.infrastructure.di

import org.gradle.api.Project
import org.gradle.internal.service.ServiceRegistry
import org.gradle.logging.StyledTextOutputFactory
import pl.allegro.tech.build.axion.release.domain.ChecksResolver
import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.VersionFactory
import pl.allegro.tech.build.axion.release.domain.VersionResolver
import pl.allegro.tech.build.axion.release.domain.VersionService
import pl.allegro.tech.build.axion.release.domain.scm.ScmChangesPrinter
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.domain.scm.ScmService
import pl.allegro.tech.build.axion.release.infrastructure.GradleAwareScmService
import pl.allegro.tech.build.axion.release.infrastructure.DryRepository
import pl.allegro.tech.build.axion.release.infrastructure.git.GitChangesPrinter
import pl.allegro.tech.build.axion.release.infrastructure.git.GitRepository

class Context {

    private final Map instances = [:]

    public Context(Project project) {
        initialize(project)
    }

    private void initialize(Project project) {
        instances[VersionFactory] = new VersionFactory()
        instances[ScmRepository] = new ScmRepositoryFactory().create(project, config(project).repository)
        instances[VersionService] = new VersionService(new VersionResolver(get(ScmRepository), get(VersionFactory)))
    }

    private <T> T get(Class<T> clazz) {
        return (T) instances[clazz]
    }
    
    VersionConfig config(Project project) {
        return project.extensions.getByType(VersionConfig)
    }

    ScmRepository repository(Project project) {
        return config(project).dryRun ? new DryRepository(get(ScmRepository), project.logger) : get(ScmRepository)
    }

    ScmService scmService(Project project) {
        return new GradleAwareScmService(project, config(project).repository, repository(project))
    }
    
    VersionFactory versionFactory() {
        return get(VersionFactory)
    }
    
    LocalOnlyResolver localOnlyResolver(Project project) {
        return new LocalOnlyResolver(config(project), project)
    }

    ChecksResolver checksResolver(Project project) {
        return new ChecksResolver(config(project).checks, project)
    }

    VersionService versionService() {
        return get(VersionService)
    }

    ScmChangesPrinter changesPrinter(ServiceRegistry services) {
        return new GitChangesPrinter(
                get(ScmRepository) as GitRepository,
                services.get(StyledTextOutputFactory).create(ScmChangesPrinter)
        )
    }
}
