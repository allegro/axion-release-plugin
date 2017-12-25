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

    void dropTag(String tagName) {
        try {
            repository.dropTag(tagName)
        } catch (ScmException e) {
            logger.quiet("Exception occurred during removing the tag: ${e.message}")
            throw e
        }
    }

    ScmPushResult push() {
        if (localOnlyResolver.localOnly(this.remoteAttached())) {
            logger.quiet("Changes made to local repository only")
            return new ScmPushResult(true, Optional.empty())
        }

        try {
            logger.quiet("Pushing all to remote: ${scmProperties.remote}")
            return repository.push(scmProperties.identity, scmProperties.pushOptions())
        } catch (ScmException e) {
            logger.quiet("Exception occurred during push: ${e.message}")
            throw e
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
