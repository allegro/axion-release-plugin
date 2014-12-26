package pl.allegro.tech.build.axion.release.domain.scm

import org.gradle.api.logging.Logger
import pl.allegro.tech.build.axion.release.domain.RepositoryConfig

interface ScmRepositoryFactory {

    ScmRepository createRepository(RepositoryConfig config,
                                   ScmIdentity identity,
                                   ScmInitializationOptions intializationOptions,
                                   Logger logger)

}
