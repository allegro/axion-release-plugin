package pl.allegro.tech.build.axion.release.domain.scm

import java.util.regex.Pattern

import groovy.lang.Closure;

interface ScmRepository {
    public static final Closure<String> LAST_TAG_SELECTOR = { List<String> tags ->
        tags && tags.size() > 0 ? tags[-1] : null
    }

    void fetchTags(ScmIdentity identity, String remoteName)

    void tag(String tagName)

    void push(ScmIdentity identity, ScmPushOptions pushOptions)

    void commit(List patterns, String message)

    void attachRemote(String remoteName, String url)

    String currentBranch()

    ScmPosition currentPosition(Pattern tagPattern)

    ScmPosition currentPosition(Pattern tagPattern, Closure<String> tagSelector)

    ScmPosition currentPosition(Pattern tagPattern, Pattern inversePattern, Closure<String> tagSelector)

    boolean remoteAttached(String remoteName);

    boolean checkUncommittedChanges()

    boolean checkAheadOfRemote()

    List<String> lastLogMessages(int messageCount)
}
