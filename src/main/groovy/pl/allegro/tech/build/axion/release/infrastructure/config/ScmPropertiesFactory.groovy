package pl.allegro.tech.build.axion.release.infrastructure.config

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties

class ScmPropertiesFactory {

    private static final String ATTACH_REMOTE_PROPERTY = 'release.attachRemote'

    private static final String FETCH_TAGS_PROPERTY = 'release.fetchTags'

    private static final String RELEASE_PUSH_TAGS_ONLY_PROPERTY = 'release.pushTagsOnly'

    private static final String DISABLE_SSH_AGENT = 'release.disableSshAgent'

    private static final String OVERRIDDEN_BRANCH_NAME = 'release.overriddenBranchName'

    static ScmProperties create(Project project, VersionConfig config) {
        return new ScmProperties(
                config.repository.type,
                config.repository.directory,
                config.repository.remote,
                (boolean) project.hasProperty(RELEASE_PUSH_TAGS_ONLY_PROPERTY) ? true : config.repository.pushTagsOnly,
                project.hasProperty(FETCH_TAGS_PROPERTY),
                project.hasProperty(ATTACH_REMOTE_PROPERTY),
                (String) (project.hasProperty(ATTACH_REMOTE_PROPERTY) ? project.property(ATTACH_REMOTE_PROPERTY) : null),
                (String) (project.hasProperty(OVERRIDDEN_BRANCH_NAME) ? project.property(OVERRIDDEN_BRANCH_NAME) : null),
                ScmIdentityFactory.create(config.repository, project.hasProperty(DISABLE_SSH_AGENT))
        )
    }

}
