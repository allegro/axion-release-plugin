package pl.allegro.tech.build.axion.release.domain.scm

import java.util.regex.Pattern

interface ScmRepository {

    void fetchTags(ScmIdentity identity, String remoteName)

    void tag(String tagName)

    void push(ScmIdentity identity, String remoteName)

    void commit(String message)

    void attachRemote(String remoteName, String url)

    ScmPosition currentPosition(Pattern tagPattern)

    boolean remoteAttached(String remoteName);

    boolean checkUncommittedChanges()

    boolean checkAheadOfRemote()

    List<String> lastLogMessages(int messageCount)
}
