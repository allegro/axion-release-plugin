package pl.allegro.tech.build.axion.release.infrastructure

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushOptions
import pl.allegro.tech.build.axion.release.domain.scm.ScmPushResult
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.domain.scm.TagsOnCommit

import java.util.regex.Pattern

class DummyRepository implements ScmRepository {

    private static final Logger logger = LoggerFactory.getLogger(DummyRepository)

    DummyRepository() {
    }

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
    void dropTag(String tagName) {
        log('drop tag')
    }

    @Override
    ScmPushResult push(ScmIdentity identity, ScmPushOptions pushOptions) {
        log('push')
        return new ScmPushResult(true, Optional.empty())
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
    ScmPosition currentPosition() {
        return new ScmPosition('', '', 'master')
    }

    @Override
    ScmPosition positionOfLastChangeIn(String path, List<String> excludeSubFolders) {
        return new ScmPosition('', '', 'master')
    }

    @Override
    Boolean isIdenticalForPath(String path, String latestChangeRevision, String tagCommitRevision) {
        return false
    }

    @Override
    TagsOnCommit latestTags(Pattern pattern) {
        logger.quiet("Could not resolve current position on uninitialized repository, returning default")
        return new TagsOnCommit(null, [])
    }

    @Override
    TagsOnCommit latestTags(Pattern pattern, String sinceCommit) {
        logger.quiet("Could not resolve current position on uninitialized repository, returning default")
        return new TagsOnCommit(null, [])
    }

    @Override
    List<TagsOnCommit> taggedCommits(Pattern pattern) {
        logger.quiet("Could not resolve current position on uninitialized repository, returning default")
        return [new TagsOnCommit(null, [])]
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
    boolean isLegacyDefTagnameRepo() {
        log('is legacy DefTagname Repository')
        return false
    }

    @Override
    List<String> lastLogMessages(int messageCount) {
        log('last log messages')
        return null
    }
}

