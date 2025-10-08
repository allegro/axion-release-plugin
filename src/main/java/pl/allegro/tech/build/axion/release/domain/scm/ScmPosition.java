package pl.allegro.tech.build.axion.release.domain.scm;

import org.gradle.api.tasks.Input;

public class ScmPosition {

    private final String revision;
    private final String shortRevision;
    private final String branch;
    private final boolean isClean;
    private final boolean isReleaseBranch;
    private final boolean isTagRef;

    public ScmPosition(String revision, String shortRevision, String branch, boolean isClean, boolean isReleaseBranch, boolean isTagRef) {
        this.revision = revision;
        this.shortRevision = shortRevision;
        this.branch = branch;
        this.isClean = isClean;
        this.isReleaseBranch = isReleaseBranch;
        this.isTagRef = isTagRef;
    }

    public ScmPosition(String revision, String shortRevision, String branch, boolean isClean, boolean isReleaseBranch) {
        this(revision, shortRevision, branch, isClean, isReleaseBranch, false);
    }

    public ScmPosition(String revision, String shortRevision, String branch, boolean isClean) {
        this(revision, shortRevision, branch, isClean, false, false);
    }

    public ScmPosition(String revision, String shortRevision, String branch) {
        this(revision, shortRevision, branch, true, false, false);
    }

    public ScmPosition(String revision, String branch, boolean isClean, boolean isReleaseBranch, boolean isTagRef) {
        this.revision = revision;
        if (revision.length() > 7) {
            this.shortRevision = revision.substring(0, 7);
        } else {
            this.shortRevision = "";
        }
        this.branch = branch;
        this.isClean = isClean;
        this.isReleaseBranch = isReleaseBranch;
        this.isTagRef = isTagRef;
    }

    public ScmPosition(String revision, String branch, boolean isClean, boolean isReleaseBranch) {
        this(revision, branch, isClean, isReleaseBranch, false);
    }

    public ScmPosition(String revision, String branch, boolean isClean) {
        this(revision, branch, isClean, false, false);
    }

    public ScmPosition(String revision, String branch) {
        this(revision, branch, true, false, false);
    }

    @Override
    public String toString() {
        return "ScmPosition[revision = " + revision
            + ", shortRevision = " + shortRevision
            + ", branch = " + branch
            + ", isClean = " + isClean
            + ", isReleaseBranch = " + isReleaseBranch
            + ", isTagRef = " + isTagRef + "]";
    }

    @Input
    public String getRevision() {
        return revision;
    }

    @Input
    public String getShortRevision() {
        return shortRevision;
    }

    @Input
    public String getBranch() {
        return branch;
    }

    @Input
    public boolean getIsClean() {
        return isClean;
    }

    @Input
    public boolean getIsReleaseBranch() {
        return isReleaseBranch;
    }

    @Input
    public boolean getIsTagRef() {
        return isTagRef;
    }
}
