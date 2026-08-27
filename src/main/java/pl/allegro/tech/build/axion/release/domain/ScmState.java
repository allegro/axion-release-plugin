package pl.allegro.tech.build.axion.release.domain;

import java.util.function.BooleanSupplier;

public class ScmState {

    private final boolean onReleaseTag;
    private final boolean onNextVersionTag;
    private final boolean noReleaseTagsFound;
    private final BooleanSupplier uncommittedChanges;

    private Boolean memoizedUncommittedChanges;

    public ScmState(boolean onReleaseTag, boolean onNextVersionTag, boolean noReleaseTagsFound, BooleanSupplier uncommittedChanges) {
        this.onReleaseTag = onReleaseTag;
        this.onNextVersionTag = onNextVersionTag;
        this.noReleaseTagsFound = noReleaseTagsFound;
        this.uncommittedChanges = uncommittedChanges;
    }

    /**
     * @deprecated use {@link #ScmState(boolean, boolean, boolean, BooleanSupplier)} - scanning the working tree
     * is expensive and is not needed when uncommitted changes are ignored
     */
    @Deprecated
    public ScmState(boolean onReleaseTag, boolean onNextVersionTag, boolean noReleaseTagsFound, boolean hasUncommittedChanges) {
        this(onReleaseTag, onNextVersionTag, noReleaseTagsFound, () -> hasUncommittedChanges);
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
        if (memoizedUncommittedChanges == null) {
            memoizedUncommittedChanges = uncommittedChanges.getAsBoolean();
        }
        return memoizedUncommittedChanges;
    }

    @Override
    public String toString() {
        return "ScmState{" +
            "onReleaseTag=" + onReleaseTag +
            ", onNextVersionTag=" + onNextVersionTag +
            ", noReleaseTagsFound=" + noReleaseTagsFound +
            // printing the state must stay cheap, so this does not force the check
            ", hasUncommittedChanges=" + (memoizedUncommittedChanges == null ? "<not checked>" : memoizedUncommittedChanges) +
            '}';
    }
}
