package pl.allegro.tech.build.axion.release.infrastructure.di

import org.gradle.api.file.Directory
import pl.allegro.tech.build.axion.release.domain.*
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHooksRunner
import pl.allegro.tech.build.axion.release.domain.properties.Properties
import pl.allegro.tech.build.axion.release.domain.scm.ScmChangesPrinter
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.domain.scm.ScmService
import pl.allegro.tech.build.axion.release.infrastructure.NoOpRepository
import pl.allegro.tech.build.axion.release.infrastructure.config.LocalOnlyResolverFactory
import pl.allegro.tech.build.axion.release.infrastructure.config.RulesFactory
import pl.allegro.tech.build.axion.release.infrastructure.config.ScmPropertiesFactory
import pl.allegro.tech.build.axion.release.infrastructure.git.GitChangesPrinter
import pl.allegro.tech.build.axion.release.infrastructure.git.GitRepository

class VersionResolutionContext {

    private final Properties rules

    private final ScmRepository scmRepository

    private final ScmProperties scmProperties

    private final LocalOnlyResolver localOnlyResolver

    private final VersionService versionService

    private VersionResolutionContext(Properties rules, ScmRepository scmRepository,
                                     ScmProperties scmProperties, File projectRoot,
                                     LocalOnlyResolver localOnlyResolver) {
        this.rules = rules
        this.scmRepository = scmRepository
        this.scmProperties = scmProperties
        this.localOnlyResolver = localOnlyResolver
        this.versionService = new VersionService(new VersionResolver(scmRepository,
            scmProperties.directory.toPath().relativize(projectRoot.toPath()).toString()))
    }

    static VersionResolutionContext create(VersionConfig versionConfig, Directory projectDirectory) {
        ScmProperties scmProperties = ScmPropertiesFactory.create(versionConfig)
        ScmRepository scmRepository = ScmRepositoryFactory.create(scmProperties)

        return new VersionResolutionContext(
            RulesFactory.create(versionConfig, scmRepository),
            scmRepository,
            scmProperties,
            projectDirectory.asFile,
            LocalOnlyResolverFactory.create(versionConfig)
        )
    }

    Properties rules() {
        return rules
    }

    ScmRepository repository() {
        return rules.dryRun ? new NoOpRepository(scmRepository) : scmRepository
    }

    ScmService scmService() {
        return new ScmService(localOnlyResolver(), scmProperties, repository())
    }

    LocalOnlyResolver localOnlyResolver() {
        return localOnlyResolver
    }

    VersionService versionService() {
        return versionService
    }

    Releaser releaser() {
        return new Releaser(
            versionService(),
            scmService(),
            new ReleaseHooksRunner(versionService(), scmService())
        )
    }

    ScmChangesPrinter changesPrinter() {
        return new GitChangesPrinter(scmRepository as GitRepository)
    }
}
