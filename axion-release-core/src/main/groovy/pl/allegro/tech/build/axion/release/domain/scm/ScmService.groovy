package pl.allegro.tech.build.axion.release.domain.scm

import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver
import pl.allegro.tech.build.axion.release.domain.logging.ReleaseLogger

class ScmService {

    private static final ReleaseLogger logger = ReleaseLogger.Factory.logger(ScmService)

    private final LocalOnlyResolver localOnlyResolver

    private final ScmProperties scmProperties

    private ScmRepository repository

    ScmService(LocalOnlyResolver localOnlyResolver, ScmProperties scmProperties, ScmRepository repository) {
        this.localOnlyResolver = localOnlyResolver
        this.scmProperties = scmProperties
        this.repository = repository
    }

    void tag(String tagName) {
        repository.tag(tagName)
    }

    void push() {
        if (!localOnlyResolver.localOnly(this.remoteAttached())) {
            logger.quiet("Pushing all to remote: ${scmProperties.remote}")
            repository.push(scmProperties.identity, scmProperties.pushOptions())
        } else {
            logger.quiet("Changes made to local repository only")
        }
    }

    void commit(List patterns, String message) {
        repository.commit(patterns, message)
    }

    boolean remoteAttached() {
        return repository.remoteAttached(scmProperties.remote)
    }

    List<String> lastLogMessages(int messageCount) {
        return repository.lastLogMessages(messageCount)
    }
}
