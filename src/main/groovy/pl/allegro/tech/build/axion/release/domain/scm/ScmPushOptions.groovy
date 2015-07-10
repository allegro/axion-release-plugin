package pl.allegro.tech.build.axion.release.domain.scm

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.domain.RepositoryConfig

class ScmPushOptions {

    private static final String RELEASE_PUSH_TAGS_ONLY_PROPERTY = 'release.pushTagsOnly'
    
    final String remote
    
    final boolean tagsOnly
    
    ScmPushOptions(String remote, boolean tagsOnly) {
        this.remote = remote
        this.tagsOnly = tagsOnly
    }
    
    static ScmPushOptions fromProject(Project project, RepositoryConfig config) {
        return new ScmPushOptions(
                config.remote,
                project.hasProperty(RELEASE_PUSH_TAGS_ONLY_PROPERTY) ? true : config.pushTagsOnly
        );
    }
    
}
