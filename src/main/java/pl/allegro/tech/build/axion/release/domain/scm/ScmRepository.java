package pl.allegro.tech.build.axion.release.domain.scm;

import java.util.List;
import java.util.regex.Pattern;

public interface ScmRepository {

    void fetchTags(ScmIdentity identity, String remoteName);

    void tag(String tagName);

    void dropTag(String tagName);

    ScmPushResult push(ScmIdentity identity, ScmPushOptions pushOptions);

    void branch(String name);

    void checkout(String name);

    void commit(List<String> patterns, String message);

    void attachRemote(String remoteName, String url);

    ScmPosition currentPosition();

    ScmPosition positionOfLastChangeIn(String path, List<String> excludeSubFolders);

    TagsOnCommit latestTags(Pattern pattern);

    TagsOnCommit latestTags(Pattern pattern, String sinceCommit);

    List<TagsOnCommit> taggedCommits(Pattern pattern);

    List<TagsOnCommit> taggedCommitsGlobally(Pattern pattern);

    boolean remoteAttached(String remoteName);

    boolean checkUncommittedChanges();

    boolean checkAheadOfRemote();

    List<String> lastLogMessages(int messageCount);
}
