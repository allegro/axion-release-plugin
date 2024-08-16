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

    public static TaggedCommits fromLatestCommit(ScmRepository repository, List<Pattern> relaseTagPatterns, ScmPosition latestTagPosition) {
        TagsOnCommit latestTags = repository.latestTags(relaseTagPatterns);
        return new TaggedCommits(latestTagPosition, Arrays.asList(latestTags));
    }

    public static TaggedCommits fromAllCommits(ScmRepository repository, List<Pattern> releaseTagPatterns, ScmPosition latestTagPosition) {
        List<TagsOnCommit> taggedCommits = repository.taggedCommits(releaseTagPatterns);
        return new TaggedCommits(latestTagPosition, taggedCommits);
    }

    public static TaggedCommits fromLatestCommitBeforeNextVersion(ScmRepository repository, List<Pattern> releaseTagPatterns, Pattern nextVersionTagPattern, ScmPosition latestTagPosition) {
        TagsOnCommit previousTags = repository.latestTags(releaseTagPatterns);
        while (previousTags.hasOnlyMatching(nextVersionTagPattern)) {
            previousTags = repository.latestTags(releaseTagPatterns, previousTags.getCommitId());
        }
        return new TaggedCommits(latestTagPosition, Arrays.asList(previousTags));
    }

    public List<TagsOnCommit> getCommits() {
        return commits;
    }

    public boolean isLatestCommit(String revision) {
        return latestCommitRevision == null ? false : latestCommitRevision.equals(revision);
    }
}
