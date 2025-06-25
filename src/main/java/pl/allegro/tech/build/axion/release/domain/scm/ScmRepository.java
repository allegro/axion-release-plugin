package pl.allegro.tech.build.axion.release.domain.scm;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public interface ScmRepository {

    void fetchTags(ScmIdentity identity, String remoteName);

    void tag(String tagName);

    void dropTag(String tagName);

    ScmPushResult push(ScmIdentity identity, ScmPushOptions pushOptions);

    void commit(List<String> patterns, String message);

    void attachRemote(String remoteName, String url);

    ScmPosition currentPosition();

    ScmPosition positionOfLastChangeIn(String path, List<String> excludeSubFolders, Set<String> dependenciesFolders);

    Boolean isIdenticalForPath(String path, String latestChangeRevision, String tagCommitRevision);

    TagsOnCommit latestTags(List<Pattern> patterns);

    TagsOnCommit latestTags(List<Pattern> patterns, String sinceCommit);

    List<TagsOnCommit> taggedCommits(List<Pattern> patterns);

    boolean remoteAttached(String remoteName);

    boolean checkUncommittedChanges();

    int numberOfCommitsAheadOrBehindRemote();

    boolean isLegacyDefTagnameRepo();

    List<String> lastLogMessages(int messageCount);
}
