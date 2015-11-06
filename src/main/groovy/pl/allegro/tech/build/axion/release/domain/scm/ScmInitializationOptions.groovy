package pl.allegro.tech.build.axion.release.domain.scm

import org.gradle.api.Project

class ScmInitializationOptions {

    final String remote

    final boolean fetchTags

    final boolean attachRemote

    final String remoteUrl

    private static final String ATTACH_REMOTE_PROPERTY = 'release.attachRemote'

    private static final String FETCH_TAGS_PROPERTY = 'release.fetchTags'

    ScmInitializationOptions(String remote, boolean fetchTags, boolean attachRemote, String remoteUrl) {
        this.remote = remote
        this.fetchTags = fetchTags
        this.attachRemote = attachRemote
        this.remoteUrl = remoteUrl
    }

    static ScmInitializationOptions fromProject(Project project, String remote) {
        return new ScmInitializationOptions(
                remote,
                project.hasProperty(FETCH_TAGS_PROPERTY),
                project.hasProperty(ATTACH_REMOTE_PROPERTY),
                (String) (project.hasProperty(ATTACH_REMOTE_PROPERTY) ? project.property(ATTACH_REMOTE_PROPERTY) : null)
        )
    }

}
