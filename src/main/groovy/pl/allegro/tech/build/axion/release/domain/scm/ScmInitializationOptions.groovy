package pl.allegro.tech.build.axion.release.domain.scm

import org.gradle.api.Project

class ScmInitializationOptions {

    final String remote

    final boolean fetchTags

    final boolean attachRemote

    final String remoteUrl

    static final String ATTACH_REMOTE_PROPERTY = 'release.attachRemote'

    static final String FETCH_TAGS_PROPERTY = 'release.fetchTags'

    ScmInitializationOptions(String remote, boolean fetchTags, boolean attachRemote, String remoteUrl) {
        this.remote = remote
        this.fetchTags = fetchTags
        this.attachRemote = attachRemote
        this.remoteUrl = remoteUrl
    }

    static Map<String, ?> extractParameters(Project project) {
        return [ATTACH_REMOTE_PROPERTY : extractOptionalParameter(project, ATTACH_REMOTE_PROPERTY),
                FETCH_TAGS_PROPERTY : extractOptionalParameter(project, FETCH_TAGS_PROPERTY)]
    }

    private static def extractOptionalParameter(Project project, String paramName) {
        return (String) (project.hasProperty(paramName) ? project.property(paramName) : null)
    }

    static ScmInitializationOptions fromProject(Project project, String remote) {
        return fromProject(extractParameters(project), remote)
    }

    static ScmInitializationOptions fromProject(Map<String, ?> properties, String remote) {
        return new ScmInitializationOptions(
                remote,
                properties.containsKey(FETCH_TAGS_PROPERTY),
                properties.containsKey(ATTACH_REMOTE_PROPERTY),
                (String) (properties.containsKey(ATTACH_REMOTE_PROPERTY) ? properties.get(ATTACH_REMOTE_PROPERTY) : null)
        )
    }

}
