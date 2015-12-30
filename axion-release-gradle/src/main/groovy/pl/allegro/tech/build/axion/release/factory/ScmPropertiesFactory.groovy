package pl.allegro.tech.build.axion.release.factory

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.config.VersionConfig
import pl.allegro.tech.build.axion.release.domain.scm.ScmProperties

class ScmPropertiesFactory {

    private static final String ATTACH_REMOTE_PROPERTY = 'release.attachRemote'

    private static final String FETCH_TAGS_PROPERTY = 'release.fetchTags'

    private static final String RELEASE_PUSH_TAGS_ONLY_PROPERTY = 'release.pushTagsOnly'

    static ScmProperties create(Project project, VersionConfig config) {
        return new ScmProperties(
                config.repository.type,
                config.repository.directory,
                config.repository.remote,
                (boolean) project.hasProperty(RELEASE_PUSH_TAGS_ONLY_PROPERTY) ? true : config.repository.pushTagsOnly,
                project.hasProperty(FETCH_TAGS_PROPERTY),
                project.hasProperty(ATTACH_REMOTE_PROPERTY),
                (String) (project.hasProperty(ATTACH_REMOTE_PROPERTY) ? project.property(ATTACH_REMOTE_PROPERTY) : null),
                ScmIdentityFactory.create(config.repository)
        )
    }

}
