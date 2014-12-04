package pl.allegro.tech.build.axion.release.infrastructure

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.VersionResolver
import pl.allegro.tech.build.axion.release.domain.VersionService
import pl.allegro.tech.build.axion.release.domain.scm.*

/**
 * Poor mans solution to Gradle DI problems (can't use DI to anything else than inner Gradle services).
 */
class ComponentFactory {

    private static final ScmRepositoryFactory repositoryFactory = new TypedScmRepositoryFactory()

    private static final ScmIdentityResolver identityResolver = new ScmIdentityResolver()

    private static ScmRepository repository

    static VersionConfig versionConfig(Project project, String extensionName) {
        return project.extensions.create(extensionName, VersionConfig, project)
    }

    static VersionService versionService(Project project, VersionConfig versionConfig) {
        project.logger.warn("$versionConfig.repositoryDir")
        ScmRepository repository = scmRepository(project, versionConfig)
        return new VersionService(new VersionResolver(repository))
    }

    static ScmRepository scmRepository(Project project, VersionConfig versionConfig) {
        if (repository == null) {
            ScmIdentity identity = identityResolver.resolve(project)
            ScmInitializationOptions initializationOptions = ScmInitializationOptions.fromProject(versionConfig.remote, project)
            repository = repositoryFactory.createRepository(versionConfig.repository,
                    versionConfig.repositoryDir,
                    identity,
                    initializationOptions,
                    project.logger)
        }

        return repository
    }
}
