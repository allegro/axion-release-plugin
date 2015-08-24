package pl.allegro.tech.build.axion.release.infrastructure

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver
import pl.allegro.tech.build.axion.release.domain.RepositoryConfig
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentityResolver
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushOptions
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

class GradleAwareScmService implements ScmService {

    private final LocalOnlyResolver localOnlyResolver

    private final Project project

    private final RepositoryConfig config

    private ScmRepository repository

    GradleAwareScmService(Project project, RepositoryConfig config, LocalOnlyResolver localOnlyResolver, ScmRepository repository) {
        this.project = project
        this.localOnlyResolver = localOnlyResolver
        this.config = config
        this.repository = repository
    }

    @Override
    void tag(String tagName) {
        repository.tag(tagName)
    }

    @Override
    void push() {
        if (!localOnlyResolver.localOnly(this.remoteAttached())) {
            project.logger.quiet("Pushing all to remote: ${config.remote}")
            repository.push(
                    ScmIdentityResolver.resolve(config),
                    ScmPushOptions.fromProject(project, config)
            )
        } else {
            project.logger.quiet("Changes made to local repository only")
        }
    }

    @Override
    void commit(List patterns, String message) {
        repository.commit(patterns, message)
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
