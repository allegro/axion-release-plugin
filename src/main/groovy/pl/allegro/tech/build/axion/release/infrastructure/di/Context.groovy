package pl.allegro.tech.build.axion.release.infrastructure.di

import pl.allegro.tech.build.axion.release.domain.*
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHooksRunner
import pl.allegro.tech.build.axion.release.domain.properties.Properties
import pl.allegro.tech.build.axion.release.domain.scm.ScmChangesPrinter
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.domain.scm.ScmService
import pl.allegro.tech.build.axion.release.infrastructure.DryRepository
import pl.allegro.tech.build.axion.release.infrastructure.git.GitChangesPrinter
import pl.allegro.tech.build.axion.release.infrastructure.git.GitRepository

class Context {

    private final Map instances = [:]

    private final Properties rules

    private final ScmRepository scmRepository

    private final ScmProperties scmProperties

    private final LocalOnlyResolver localOnlyResolver

    public Context(Properties rules, ScmRepository scmRepository, ScmProperties scmProperties, LocalOnlyResolver localOnlyResolver) {
        this.rules = rules
        this.scmRepository = scmRepository
        this.scmProperties = scmProperties
        this.localOnlyResolver = localOnlyResolver

        instances[ScmRepository] = scmRepository
        instances[VersionService] = new VersionService(new VersionResolver(scmRepository))

    }

    private <T> T get(Class<T> clazz) {
        return (T) instances[clazz]
    }

    Properties rules() {
        return rules
    }

    ScmRepository repository() {
        return rules.dryRun ? new DryRepository(get(ScmRepository)) : get(ScmRepository)
    }

    ScmService scmService() {
        return new ScmService(localOnlyResolver(), scmProperties, repository())
    }

    LocalOnlyResolver localOnlyResolver() {
        return localOnlyResolver
    }

    VersionService versionService() {
        return get(VersionService)
    }

    Releaser releaser() {
        return new Releaser(
                versionService(),
                scmService(),
                new ReleaseHooksRunner(versionService(), scmService())
        )
    }

    ScmChangesPrinter changesPrinter() {
        return new GitChangesPrinter(get(ScmRepository) as GitRepository)
    }
}
