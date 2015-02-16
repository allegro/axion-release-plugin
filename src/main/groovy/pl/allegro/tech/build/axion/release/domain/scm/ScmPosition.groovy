package pl.allegro.tech.build.axion.release.domain.scm

class ScmPosition {

    final String branch

    final String latestTag

    final boolean onTag

    ScmPosition(String branch, String latestTag, boolean onTag) {
        this.branch = branch
        this.latestTag = latestTag
        this.onTag = onTag
    }

    static ScmPosition defaultPosition() {
        return new ScmPosition('master', null, false)
    }

    static ScmPosition onTag(String tagName) {
        return new ScmPosition('master', tagName, true)
    }
    
    boolean tagless() {
        return latestTag == null
    }

    ScmPosition asOnTagPosition() {
        return new ScmPosition(branch, latestTag, latestTag != null)
    }

    ScmPosition asNotOnTagPosition() {
        return new ScmPosition(branch, latestTag, false)
    }
    
    @Override
    public String toString() {
        return "ScmPosition[branch = $branch, latestTag = $latestTag, onTag = $onTag]"
    }
}
