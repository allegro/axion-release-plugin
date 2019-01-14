package pl.allegro.tech.build.axion.release.domain.scm;

public class ScmPosition {

    private final String revision;
    private final String shortRevision;
    private final String branch;

    public ScmPosition(String revision, String shortRevision, String branch) {
        this.revision = revision;
        this.shortRevision = shortRevision;
        this.branch = branch;
    }

    public ScmPosition(String revision, String branch) {
        this.revision = revision;
        if (revision.length() > 7) {
            this.shortRevision = revision.substring(0, 7);
        } else {
            this.shortRevision = "";
        }
        this.branch = branch;
    }

    @Override
    public String toString() {
        return "ScmPosition[revision = " + revision
            + ", shortRevision = " + shortRevision
            + ", branch = " + branch + "]";
    }

    public String getRevision() {
        return revision;
    }

    public String getShortRevision() {
        return shortRevision;
    }

    public String getBranch() {
        return branch;
    }
}
