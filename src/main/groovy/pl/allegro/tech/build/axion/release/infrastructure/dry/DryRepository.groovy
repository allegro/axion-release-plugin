package pl.allegro.tech.build.axion.release.infrastructure.dry

import org.gradle.api.logging.Logger
import pl.allegro.tech.build.axion.release.domain.scm.ScmInitializationOptions
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository

class DryRepository implements ScmRepository {
    
    private final ScmRepository delegateRepository
    
    private final Logger logger

    DryRepository(ScmRepository delegateRepository, Logger logger) {
        this.delegateRepository = delegateRepository
        this.logger = logger
    }

    @Override
    void initialize(ScmInitializationOptions options) {
        delegateRepository.initialize(options)
    }

    @Override
    void fetchTags() {
        log('fetching tags from remote')
        delegateRepository.fetchTags()
    }

    @Override
    void tag(String tagName) {
        log("creating tag with name: $tagName")
    }

    @Override
    void push(String remoteName) {
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
    ScmPosition currentPosition(String tagPrefix) {
        ScmPosition position = delegateRepository.currentPosition(tagPrefix)
        log("scm position: $position")
        return position
    }

    @Override
    boolean remoteAttached(String remoteName) {
        return true
    }

    @Override
    boolean checkUncommitedChanges() {
        boolean uncommitedChanges = delegateRepository.checkUncommitedChanges()
        log("uncommited changes: $uncommitedChanges")
        return uncommitedChanges
    }

    @Override
    boolean checkAheadOfRemote() {
        boolean aheadOfRemote = delegateRepository.checkAheadOfRemote()
        log("ahead of remote: $aheadOfRemote")
        return aheadOfRemote
    }


    private void log(String msg) {
        logger.quiet("DRY-RUN: $msg")
    }
}
