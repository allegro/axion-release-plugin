package pl.allegro.tech.build.axion.release.infrastructure

import pl.allegro.tech.build.axion.release.domain.logging.ReleaseLogger
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushOptions
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushResult
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.domain.scm.TagsOnCommit

import java.util.regex.Pattern

class DryRepository implements ScmRepository {

    private static final ReleaseLogger logger = ReleaseLogger.Factory.logger(DryRepository)

    private final ScmRepository delegateRepository

    DryRepository(ScmRepository delegateRepository) {
        this.delegateRepository = delegateRepository
    }

    @Override
    void fetchTags(ScmIdentity identity, String remoteName) {
        log("fetching tags from remote")
        delegateRepository.fetchTags(identity, remoteName)
    }

    @Override
    void tag(String tagName) {
        log("creating tag with name: $tagName")
    }

    @Override
    void dropTag(String tagName) {
        log("dropping tag with name: $tagName")
    }

    @Override
    ScmPushResult push(ScmIdentity identity, ScmPushOptions pushOptions) {
        log("pushing to remote: ${pushOptions.remote}")
        return new ScmPushResult(true, Optional.empty())
    }

    @Override
    void commit(List patterns, String message) {
        log("commiting files matching $patterns with message: $message")
    }

    @Override
    void attachRemote(String remoteName, String url) {
        log("attaching remote: $remoteName")
    }

    @Override
    ScmPosition currentPosition() {
        return delegateRepository.currentPosition()
    }

    @Override
    ScmPosition positionOfLastChangeIn(String path, List<String> excludeSubFolders, Set<String> dependenciesFolders) {
        return delegateRepository.positionOfLastChangeIn(path)
    }

    @Override
    Boolean isIdenticalForPath(String path, String latestChangeRevision, String tagCommitRevision) {
        return delegateRepository.isIdenticalForPath(path, latestChangeRevision, tagCommitRevision)
    }

    @Override
    TagsOnCommit latestTags(Pattern pattern) {
        TagsOnCommit tags = delegateRepository.latestTags(pattern)
        log("Latest tags: ${tags.tags}")
        return tags
    }

    @Override
    TagsOnCommit latestTags(Pattern pattern, String sinceCommit) {
        TagsOnCommit tags = delegateRepository.latestTags(pattern, sinceCommit)
        log("Latest tags: ${tags.tags}")
        return tags
    }

    @Override
    List<TagsOnCommit> taggedCommits(Pattern pattern) {
        return delegateRepository.taggedCommits(pattern)
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
    boolean isLegacyDefTagnameRepo() {
        boolean  isLegacyDefTagnameRepo = delegateRepository.isLegacyDefTagnameRepo()
        log('is legacy named repository')
        return isLegacyDefTagnameRepo
    }

    @Override
    List<String> lastLogMessages(int messageCount) {
        return delegateRepository.lastLogMessages(messageCount)
    }

    private void log(String msg) {
        logger.quiet("DRY-RUN: $msg")
    }
}
