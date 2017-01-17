package pl.allegro.tech.build.axion.release.domain.scm

import java.util.regex.Pattern

interface ScmRepository {

    void fetchTags(ScmIdentity identity, String remoteName)

    void tag(String tagName)

    void push(ScmIdentity identity, ScmPushOptions pushOptions)

    void commit(List patterns, String message)

    void attachRemote(String remoteName, String url)

    String currentBranch()

    TagsOnCommit latestTags(Pattern pattern)

    TagsOnCommit latestTags(Pattern pattern, String sinceCommit)

    boolean remoteAttached(String remoteName);

    boolean checkUncommittedChanges()

    boolean checkAheadOfRemote()

    List<String> lastLogMessages(int messageCount)
}
