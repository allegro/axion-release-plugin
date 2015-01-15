package pl.allegro.tech.build.axion.release.domain.scm

interface ScmRepository {

    void fetchTags(ScmIdentity identity, String remoteName)

    void tag(String tagName)

    void push(ScmIdentity identity, String remoteName)

    void commit(String message)

    void attachRemote(String remoteName, String url)

    ScmPosition currentPosition(String tagPrefix)

    boolean remoteAttached(String remoteName);

    boolean checkUncommittedChanges()

    boolean checkAheadOfRemote()

    List<String> lastLogMessages(int messageCount)
}
