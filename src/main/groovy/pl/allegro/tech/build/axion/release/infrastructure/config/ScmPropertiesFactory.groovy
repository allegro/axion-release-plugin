package pl.allegro.tech.build.axion.release.infrastructure.config


import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties

class ScmPropertiesFactory {
    static ScmProperties create(VersionConfig config) {
        return new ScmProperties(
            config.repository.type.get(),
            new File(config.repository.directory.get()),
            config.repository.remote.get(),
            config.repository.pushTagsOnly().get(),
            config.repository.fetchTags().get(),
            config.repository.attachRemote().isPresent(),
            config.repository.attachRemote().getOrNull(),
            config.repository.overriddenBranch().getOrNull(),
            config.repository.overriddenIsClean().getOrNull(),
            ScmIdentityFactory.create(config.repository, config.repository.disableSshAgent().get()),
            config.getUnshallowRepoOnCI().get(),
            config.getReleaseBranchNames().get(),
            config.getReleaseOnlyOnReleaseBranches().get(),
            config.getIgnoreGlobalGitConfig().get(),
            config.getUpdateProjectVersionAfterRelease().get(),
        )
    }
}
