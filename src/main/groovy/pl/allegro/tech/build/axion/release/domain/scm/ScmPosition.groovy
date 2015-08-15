package pl.allegro.tech.build.axion.release.domain.scm

class ScmPosition {

    final String branch

    final String latestTag

    final boolean onTag
    
    final boolean hasUncommittedChanges

    ScmPosition(String branch, String latestTag, boolean onTag, boolean hasUncommittedChanges = false) {
        this.branch = branch
        this.latestTag = latestTag
        this.onTag = onTag
        this.hasUncommittedChanges = hasUncommittedChanges
    }

    static ScmPosition defaultPosition() {
        return new ScmPosition('master', null, false, false)
    }

    static ScmPosition onTag(String tagName) {
        return new ScmPosition('master', tagName, true, false)
    }
    
    boolean tagless() {
        return latestTag == null
    }

    ScmPosition asOnTagPosition() {
        return new ScmPosition(branch, latestTag, latestTag != null, hasUncommittedChanges)
    }

    ScmPosition asNotOnTagPosition() {
        return new ScmPosition(branch, latestTag, false, hasUncommittedChanges)
    }
    
    @Override
    public String toString() {
        return "ScmPosition[branch = $branch, latestTag = $latestTag, onTag = $onTag]"
    }
}
