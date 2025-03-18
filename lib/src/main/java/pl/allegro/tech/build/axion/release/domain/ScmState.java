package pl.allegro.tech.build.axion.release.domain;

public class ScmState {

    private final boolean onReleaseTag;
    private final boolean onNextVersionTag;
    private final boolean noReleaseTagsFound;
    private final boolean hasUncommittedChanges;

    public ScmState(boolean onReleaseTag, boolean onNextVersionTag, boolean noReleaseTagsFound, boolean hasUncommittedChanges) {
        this.onReleaseTag = onReleaseTag;
        this.onNextVersionTag = onNextVersionTag;
        this.noReleaseTagsFound = noReleaseTagsFound;
        this.hasUncommittedChanges = hasUncommittedChanges;
    }

    public final boolean isOnReleaseTag() {
        return onReleaseTag;
    }

    public final boolean isOnNextVersionTag() {
        return onNextVersionTag;
    }

    public final boolean isNoReleaseTagsFound() {
        return noReleaseTagsFound;
    }

    public final boolean hasUncommittedChanges() {
        return hasUncommittedChanges;
    }

    @Override
    public String toString() {
        return "ScmState{" +
            "onReleaseTag=" + onReleaseTag +
            ", onNextVersionTag=" + onNextVersionTag +
            ", noReleaseTagsFound=" + noReleaseTagsFound +
            ", hasUncommittedChanges=" + hasUncommittedChanges +
            '}';
    }
}
