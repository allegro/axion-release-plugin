package pl.allegro.tech.build.axion.release.domain.scm

import org.gradle.api.logging.Logger

interface ScmRepositoryFactory {

    ScmRepository createRepository(String type,
                                   File repositoryDir,
                                   ScmIdentity identity,
                                   ScmInitializationOptions intializationOptions,
                                   Logger logger)

}
