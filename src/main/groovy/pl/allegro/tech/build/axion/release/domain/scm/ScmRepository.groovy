package pl.allegro.tech.build.axion.release.domain.scm

interface ScmRepository {

    void initialize(ScmInitializationOptions initializationOptions)

    void fetchTags()

    void tag(String tagName)

    void push(String remoteName)

    void commit(String message)

    void attachRemote(String remoteName, String url)

    ScmPosition currentPosition(String tagPrefix)

    boolean remoteAttached(String remoteName);

    boolean checkUncommitedChanges()

    boolean checkAheadOfRemote()

}
