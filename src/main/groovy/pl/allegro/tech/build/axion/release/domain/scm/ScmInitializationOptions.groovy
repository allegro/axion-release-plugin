package pl.allegro.tech.build.axion.release.domain.scm

import org.gradle.api.Project

class ScmInitializationOptions {

    final String remote

    final boolean fetchTags

    final boolean attachRemote

    final String remoteUrl

    final boolean pushTagsOnly

    private static final String ATTACH_REMOTE_PROPERTY = 'release.attachRemote'

    private static final String FETCH_TAGS_PROPERTY = 'release.fetchTags'

    ScmInitializationOptions(String remote, boolean fetchTags, boolean attachRemote, String remoteUrl, boolean pushTagsOnly) {
        this.remote = remote
        this.fetchTags = fetchTags
        this.attachRemote = attachRemote
        this.remoteUrl = remoteUrl
        this.pushTagsOnly = pushTagsOnly
    }

    static ScmInitializationOptions fromProject(Project project, String remote, boolean pushTagsOnly=false) {
        return new ScmInitializationOptions(
                remote,
                project.hasProperty(FETCH_TAGS_PROPERTY),
                project.hasProperty(ATTACH_REMOTE_PROPERTY),
                project.hasProperty(ATTACH_REMOTE_PROPERTY) ? project.property(ATTACH_REMOTE_PROPERTY) : null,
                pushTagsOnly
        )
    }

}
