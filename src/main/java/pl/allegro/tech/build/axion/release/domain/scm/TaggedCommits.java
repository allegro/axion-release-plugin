package pl.allegro.tech.build.axion.release.domain.scm;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class TaggedCommits {

    private final List<TagsOnCommit> commits;
    private final String latestCommitRevision;

    private TaggedCommits(ScmPosition latestTagPosition, List<TagsOnCommit> commits) {
        this.commits = commits;
        this.latestCommitRevision = latestTagPosition.getRevision();
    }

    public static TaggedCommits fromListOfCommits(ScmPosition latestTagPosition, List<TagsOnCommit> taggedCommits) {
        return new TaggedCommits(latestTagPosition, taggedCommits);
    }

    public static TaggedCommits fromLatestCommit(ScmRepository repository, Pattern tagPattern, ScmPosition latestTagPosition) {
        TagsOnCommit latestTags = repository.latestTags(tagPattern);
        return new TaggedCommits(latestTagPosition, Arrays.asList(latestTags));
    }

    public static TaggedCommits fromAllCommits(ScmRepository repository, Pattern tagPattern, ScmPosition latestTagPosition) {
        List<TagsOnCommit> taggedCommits = repository.taggedCommits(tagPattern);
        return new TaggedCommits(latestTagPosition, taggedCommits);
    }

    public static TaggedCommits fromPreviousCommitBeforeNextVersion(ScmRepository repository, Pattern releaseTagPattern, Pattern nextVersionTagPattern, ScmPosition latestChangePosition) {
        TagsOnCommit previousTags = repository.latestTags(releaseTagPattern, latestChangePosition.getRevision());
        while (previousTags.hasOnlyMatching(nextVersionTagPattern)) {
            previousTags = repository.latestTags(releaseTagPattern, previousTags.getCommitId());
        }
        return new TaggedCommits(latestChangePosition, Arrays.asList(previousTags));
    }

    public List<TagsOnCommit> getCommits() {
        return commits;
    }

    public boolean isLatestCommit(String revision) {
        return latestCommitRevision == null ? false : latestCommitRevision.equals(revision);
    }
}
