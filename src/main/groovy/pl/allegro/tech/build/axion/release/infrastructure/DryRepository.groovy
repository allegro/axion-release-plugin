package pl.allegro.tech.build.axion.release.infrastructure

import org.gradle.api.logging.Logger
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository

import java.util.regex.Pattern

class DryRepository implements ScmRepository {
    
    private final ScmRepository delegateRepository
    
    private final Logger logger

    DryRepository(ScmRepository delegateRepository, Logger logger) {
        this.delegateRepository = delegateRepository
        this.logger = logger
    }

    @Override
    void fetchTags(ScmIdentity identity, String remoteName) {
        log('fetching tags from remote')
        delegateRepository.fetchTags(identity, remoteName)
    }

    @Override
    void tag(String tagName) {
        log("creating tag with name: $tagName")
    }

    @Override
    void push(ScmIdentity identity, String remoteName) {
        log("pushing to remote: $remoteName")
    }

    @Override
    void commit(String message) {
        log("commiting message: $message")
    }

    @Override
    void attachRemote(String remoteName, String url) {
        log("attaching remote: $remoteName")
    }

    @Override
    ScmPosition currentPosition(Pattern tagPattern) {
        ScmPosition position = delegateRepository.currentPosition(tagPattern)
        log("scm position: $position")
        return position
    }

    @Override
    boolean remoteAttached(String remoteName) {
        return true
    }

    @Override
    boolean checkUncommittedChanges() {
        boolean uncommittedChanges = delegateRepository.checkUncommittedChanges()
        log("uncommitted changes: $uncommittedChanges")
        return uncommittedChanges
    }

    @Override
    boolean checkAheadOfRemote() {
        boolean aheadOfRemote = delegateRepository.checkAheadOfRemote()
        log("ahead of remote: $aheadOfRemote")
        return aheadOfRemote
    }

    @Override
    List<String> lastLogMessages(int messageCount) {
        return delegateRepository.lastLogMessages(messageCount)
    }

    private void log(String msg) {
        logger.quiet("DRY-RUN: $msg")
    }
}
