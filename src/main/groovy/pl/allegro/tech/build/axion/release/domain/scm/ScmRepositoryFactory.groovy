package pl.allegro.tech.build.axion.release.domain.scm

interface ScmRepositoryFactory {

    ScmRepository createRepository(String type,
                                   File repositoryDir,
                                   ScmIdentity identity,
                                   ScmInitializationOptions intializationOptions)

}
