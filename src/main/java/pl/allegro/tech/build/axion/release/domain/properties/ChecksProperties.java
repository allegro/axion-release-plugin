package pl.allegro.tech.build.axion.release.domain.properties;

public class ChecksProperties {

    private final boolean checkUncommittedChanges;
    private final boolean checkAheadOfRemote;
    private final boolean checkSnapshotDependencies;

    public ChecksProperties(
        boolean checkUncommittedChanges,
        boolean checkAheadOfRemote,
        boolean checkSnapshotDependencies
    ) {
        this.checkUncommittedChanges = checkUncommittedChanges;
        this.checkAheadOfRemote = checkAheadOfRemote;
        this.checkSnapshotDependencies = checkSnapshotDependencies;
    }

    public final boolean getCheckUncommittedChanges() {
        return checkUncommittedChanges;
    }

    public final boolean isCheckUncommittedChanges() {
        return checkUncommittedChanges;
    }

    public final boolean isCheckAheadOfRemote() {
        return checkAheadOfRemote;
    }

    public final boolean isCheckSnapshotDependencies() {
        return checkSnapshotDependencies;
    }
}
