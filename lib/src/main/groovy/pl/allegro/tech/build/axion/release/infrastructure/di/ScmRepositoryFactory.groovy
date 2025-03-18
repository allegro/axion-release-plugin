package pl.allegro.tech.build.axion.release.infrastructure.di

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepositoryUnavailableException
import pl.allegro.tech.build.axion.release.infrastructure.DummyRepository
import pl.allegro.tech.build.axion.release.infrastructure.git.GitRepository

class ScmRepositoryFactory {
    private static final Logger logger = Logging.getLogger(ScmRepositoryFactory.class);

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
