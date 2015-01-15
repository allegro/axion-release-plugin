package pl.allegro.tech.build.axion.release.domain.scm

import pl.allegro.tech.build.axion.release.domain.RepositoryConfig

interface ScmRepositoryFactory {

    ScmRepository createRepository(RepositoryConfig config, ScmInitializationOptions initializationOptions)

}
