package pl.allegro.tech.build.axion.release.domain.scm

interface ScmService {

    void push()

    void tag(String tagName)

    void commit(String message)

    boolean remoteAttached()

    List<String> lastLogMessages(int messageCount)
}
