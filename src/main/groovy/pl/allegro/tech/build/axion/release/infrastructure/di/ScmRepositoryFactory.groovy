package pl.allegro.tech.build.axion.release.infrastructure.di

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.domain.RepositoryConfig
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentityResolver
import pl.allegro.tech.build.axion.release.domain.scm.ScmInitializationOptions
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepositoryUnavailableException
import pl.allegro.tech.build.axion.release.infrastructure.DummyRepository
import pl.allegro.tech.build.axion.release.infrastructure.git.GitRepository

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ScmRepositoryFactory {

    private static final String GIT = 'git'

    private final Logger logger = LoggerFactory.getLogger(ScmRepositoryFactory.getClass())

    ScmRepository create(Map<String, ?> properties, RepositoryConfig config) {
        if(config.type != GIT) {
            throw new IllegalArgumentException("Unsupported repository type $config.type")
        }

        ScmRepository repository
        try {
            ScmInitializationOptions initializationOptions = ScmInitializationOptions.fromProject(properties, config.remote)
            ScmIdentity identity = ScmIdentityResolver.resolve(config)
            repository = new GitRepository(config.directory, identity, initializationOptions)
        }
        catch(ScmRepositoryUnavailableException exception) {
            logger.warn('Failed top open repository, trying to work without it', exception)
            repository = new DummyRepository()
        }

        return repository
    }
}
