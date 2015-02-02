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
    
    static ScmPosition notOnTag(ScmPosition position) {
        return new ScmPosition(position.branch, position.latestTag, false)
    }

    boolean tagless() {
        return latestTag == null
    }

    @Override
    public String toString() {
        return "ScmPosition[branch = $branch, latestTag = $latestTag, onTag = $onTag]"
    }
}
