package pl.allegro.tech.build.axion.release.infrastructure

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.domain.RepositoryConfig
import pl.allegro.tech.build.axion.release.domain.scm.*

class GradleAwareScmService implements ScmService {

    private final Project project
    
    private final RepositoryConfig config

    private ScmRepository repository

    GradleAwareScmService(Project project, RepositoryConfig config, ScmRepository repository) {
        this.project = project
        this.config = config
        this.repository = repository
    }

    @Override
    void tag(String tagName) {
        repository.tag(tagName)
    }

    @Override
    void push() {
        project.logger.quiet("Pushing all to remote: ${config.remote}")
        ScmIdentity identity = ScmIdentityResolver.resolve(config)
        repository.push(identity, config.remote)
    }

    @Override
    void commit(String message) {
        repository.commit(message)
    }

    @Override
    boolean remoteAttached() {
        return repository.remoteAttached(config.remote)
    }

    @Override
    List<String> lastLogMessages(int messageCount) {
        return repository.lastLogMessages(messageCount)
    }
}
