package pl.allegro.tech.build.axion.release.domain.scm;

import com.squareup.moshi.JsonClass;
import org.gradle.api.tasks.Input;

@JsonClass(generateAdapter = false)
public class ScmPosition {

    private final String revision;
    private final String shortRevision;
    private final String branch;
    private final boolean isClean;

    public ScmPosition(String revision, String shortRevision, String branch, boolean isClean) {
        this.revision = revision;
        this.shortRevision = shortRevision;
        this.branch = branch;
        this.isClean = isClean;
    }

    public ScmPosition(String revision, String shortRevision, String branch) {
        this(revision, shortRevision, branch, true);
    }

    public ScmPosition(String revision, String branch, boolean isClean) {
        this.revision = revision;
        if (revision.length() > 7) {
            this.shortRevision = revision.substring(0, 7);
        } else {
            this.shortRevision = "";
        }
        this.branch = branch;
        this.isClean = isClean;
    }

    public ScmPosition(String revision, String branch) {
        this(revision, branch, true);
    }

    @Override
    public String toString() {
        return "ScmPosition[revision = " + revision
            + ", shortRevision = " + shortRevision
            + ", branch = " + branch
            + ", isClean = " + isClean + "]";
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
}
