package pl.allegro.tech.build.axion.release.infrastructure.di

import org.gradle.api.Project
import org.gradle.internal.service.ServiceRegistry
import org.gradle.logging.StyledTextOutputFactory
import pl.allegro.tech.build.axion.release.domain.ChecksResolver
import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver
import pl.allegro.tech.build.axion.release.domain.Releaser
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.VersionFactory
import pl.allegro.tech.build.axion.release.domain.VersionResolver
import pl.allegro.tech.build.axion.release.domain.VersionService
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHooksRunner
import pl.allegro.tech.build.axion.release.domain.scm.ScmChangesPrinter
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.domain.scm.ScmService
import pl.allegro.tech.build.axion.release.infrastructure.GradleAwareScmService
import pl.allegro.tech.build.axion.release.infrastructure.DryRepository
import pl.allegro.tech.build.axion.release.infrastructure.git.GitChangesPrinter
import pl.allegro.tech.build.axion.release.infrastructure.git.GitRepository

class Context {

    private final Map instances = [:]

    private final Project project
    
    public Context(Project project) {
        this.project = project
        initialize(project)
    }

    private void initialize(Project project) {
        instances[VersionFactory] = new VersionFactory()
        instances[ScmRepository] = new ScmRepositoryFactory().create(project, config().repository)
        instances[VersionService] = new VersionService(new VersionResolver(get(ScmRepository), get(VersionFactory)))
    }

    private <T> T get(Class<T> clazz) {
        return (T) instances[clazz]
    }
    
    VersionConfig config() {
        return project.extensions.getByType(VersionConfig)
    }

    ScmRepository repository() {
        return config().dryRun ? new DryRepository(get(ScmRepository), project.logger) : get(ScmRepository)
    }

    ScmService scmService() {
        return new GradleAwareScmService(project, config().repository, localOnlyResolver(), repository())
    }
    
    VersionFactory versionFactory() {
        return get(VersionFactory)
    }
    
    LocalOnlyResolver localOnlyResolver() {
        return new LocalOnlyResolver(config(), project)
    }

    ChecksResolver checksResolver() {
        return new ChecksResolver(config().checks, project)
    }

    VersionService versionService() {
        return get(VersionService)
    }

    Releaser releaser() {
        return new Releaser(
                scmService(),
                new ReleaseHooksRunner(project.logger, config(), scmService(), config().hooks),
                project
        )
    }
    
    ScmChangesPrinter changesPrinter(ServiceRegistry services) {
        return new GitChangesPrinter(
                get(ScmRepository) as GitRepository,
                services.get(StyledTextOutputFactory).create(ScmChangesPrinter)
        )
    }
}
