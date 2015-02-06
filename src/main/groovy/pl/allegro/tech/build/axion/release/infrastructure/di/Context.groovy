package pl.allegro.tech.build.axion.release.infrastructure.di

import org.gradle.api.Project
import org.gradle.internal.service.ServiceRegistry
import org.gradle.logging.StyledTextOutputFactory
import pl.allegro.tech.build.axion.release.domain.ChecksResolver
import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver
import pl.allegro.tech.build.axion.release.domain.VersionConfig
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

    private static Context context = null

    private final Map instances = [:]

    private final VersionConfig config

    private final Project project

    private Context(Project project) {
        this.project = project
        config = project.extensions.getByType(VersionConfig)
        initialize()
    }

    static Context instance(Project project) {
        if (context == null) {
            context = new Context(project)
        }
        return context
    }

    private void initialize() {
        instances[ScmRepository] = new ScmRepositoryFactory().create(project, config.repository)
        instances[VersionService] = new VersionService(new VersionResolver(get(ScmRepository)))
    }

    private <T> T get(Class<T> clazz) {
        return (T) instances[clazz]
    }

    public VersionConfig config() {
        return config
    }

    public ScmRepository repository() {
        return config.dryRun ? new DryRepository(get(ScmRepository), project.logger) : get(ScmRepository)
    }

    public ScmService scmService() {
        return new GradleAwareScmService(project, config.repository, repository())
    }
    
    public LocalOnlyResolver localOnlyResolver() {
        return new LocalOnlyResolver(config, project)
    }

    public ChecksResolver checksResolver() {
        return new ChecksResolver(config.checks, project)
    }

    public VersionService versionService() {
        return get(VersionService)
    }

    public ScmChangesPrinter changesPrinter(ServiceRegistry services) {
        return new GitChangesPrinter(
                get(ScmRepository) as GitRepository,
                services.get(StyledTextOutputFactory).create(ScmChangesPrinter)
        )
    }
}
