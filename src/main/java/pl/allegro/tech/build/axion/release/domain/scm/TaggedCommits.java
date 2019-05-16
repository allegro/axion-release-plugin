package pl.allegro.tech.build.axion.release.domain.scm;

import java.util.List;

public class TaggedCommits {

    private List<TagsOnCommit> commits;
    private String latestCommitRevision;

    public TaggedCommits(List<TagsOnCommit> commits, String latestCommitRevision) {
        this.commits = commits;
        this.latestCommitRevision = latestCommitRevision;
    }

    public List<TagsOnCommit> getCommits() {
        return commits;
    }

    public boolean isLatestCommit(String revision) {
        return latestCommitRevision == null ? false : latestCommitRevision.equals(revision);
    }
}
