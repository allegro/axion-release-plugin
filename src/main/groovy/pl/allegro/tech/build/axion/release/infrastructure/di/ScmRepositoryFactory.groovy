package pl.allegro.tech.build.axion.release.infrastructure.di

import pl.allegro.tech.build.axion.release.domain.logging.ReleaseLogger
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties

import pl.allegro.tech.build.axion.release.domain.scm.ScmRepositoryUnavailableException
import pl.allegro.tech.build.axion.release.infrastructure.DummyRepository

class ScmRepositoryFactory {

    private static final ReleaseLogger logger = ReleaseLogger.Factory.logger(ScmRepositoryFactory)

    private static final String GIT = 'git'

    static ScmRepository create(ScmProperties properties) {
        if (properties.type != GIT) {
            throw new IllegalArgumentException("Unsupported repository type $properties.type")
        }

        ScmRepository repository
        try {
            repository = new GitRepository(properties)
        } catch (ScmRepositoryUnavailableException exception) {
            logger.warn("Failed to open repository, trying to work without it $exception.message")
            repository = new DummyRepository()
        }
        return repository
    }
}
