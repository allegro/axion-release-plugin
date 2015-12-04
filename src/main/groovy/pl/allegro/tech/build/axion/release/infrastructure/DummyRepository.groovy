package pl.allegro.tech.build.axion.release.infrastructure

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushOptions
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository

import java.util.regex.Pattern

class DummyRepository implements ScmRepository {

    private final Logger logger = LoggerFactory.getLogger(DummyRepository)

    private void log(String commandName) {
        logger.quiet("Couldn't perform $commandName command on uninitialized repository")
    }

    @Override
    void fetchTags(ScmIdentity identity, String remoteName) {
        log('fetch tags')
    }

    @Override
    void tag(String tagName) {
        log('create tag')
    }

    @Override
    void push(ScmIdentity identity, ScmPushOptions pushOptions) {
        log('push')
    }

    @Override
    void commit(List patterns, String message) {
        log('commit')
    }

    @Override
    void attachRemote(String remoteName, String url) {
        log('attach remote')
    }

    @Override
    ScmPosition currentPosition(Pattern tagPattern) {
        logger.quiet("Could not resolve current position on uninitialized repository, returning default")
        return ScmPosition.defaultPosition()
    }
    
    @Override
    ScmPosition currentPosition(Pattern tagPattern, Pattern inversePattern) {
        return currentPosition(tagPattern)
    }

    @Override
    boolean remoteAttached(String remoteName) {
        log('remote attached')
        return false
    }

    @Override
    boolean checkUncommittedChanges() {
        log('check uncommitted changes')
        return false
    }

    @Override
    boolean checkAheadOfRemote() {
        log('check ahead of remote')
        return false
    }

    @Override
    List<String> lastLogMessages(int messageCount) {
        log('last log messages')
        return null
    }
}

