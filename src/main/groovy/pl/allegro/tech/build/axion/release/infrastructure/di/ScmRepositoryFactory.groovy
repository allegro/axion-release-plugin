package pl.allegro.tech.build.axion.release.infrastructure.di

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.domain.RepositoryConfig
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentityResolver
import pl.allegro.tech.build.axion.release.domain.scm.ScmInitializationOptions
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepositoryUnavailableException
import pl.allegro.tech.build.axion.release.infrastructure.DummyRepository
import pl.allegro.tech.build.axion.release.infrastructure.git.GitRepository

class ScmRepositoryFactory {

    private static final String GIT = 'git'

    private static final String REPOSITORY_PUSH_TAGS_ONLY_FLAG = "repository.pushTagsOnly";

    ScmRepository create(Project project, RepositoryConfig config) {
        if(config.type != GIT) {
            throw new IllegalArgumentException("Unsupported repository type $config.type")
        }

        boolean pushTagsOnly = (project.hasProperty(REPOSITORY_PUSH_TAGS_ONLY_FLAG) ?
                true : config.pushTagsOnly)

        ScmRepository repository
        try {
            ScmInitializationOptions initializationOptions = ScmInitializationOptions.fromProject(project, config.remote, pushTagsOnly)
            ScmIdentity identity = ScmIdentityResolver.resolve(config)
            repository = new GitRepository(config.directory, identity, initializationOptions)
        }
        catch(ScmRepositoryUnavailableException exception) {
            project.logger.warn("Failed top open repository, trying to work without it", exception)
            repository = new DummyRepository(project.logger)
        }

        return repository
    }
}
