package pl.allegro.tech.build.axion.release.domain;

import org.gradle.api.tasks.Input;

public class ChecksConfig {
    @Input
    private boolean aheadOfRemote = true;

    @Input
    private boolean uncommittedChanges = true;

    @Input
    private boolean snapshotDependencies = true;

    public void setAheadOfRemote(boolean aheadOfRemote) {
        this.aheadOfRemote = aheadOfRemote;
    }

    public void setUncommittedChanges(boolean uncommittedChanges) {
        this.uncommittedChanges = uncommittedChanges;
    }

    public void setSnapshotDependencies(boolean snapshotDependencies) {
        this.snapshotDependencies = snapshotDependencies;
    }

    public boolean isAheadOfRemote() {
        return aheadOfRemote;
    }

    public boolean isUncommittedChanges() {
        return uncommittedChanges;
    }

    public boolean isSnapshotDependencies() {
        return snapshotDependencies;
    }
}
