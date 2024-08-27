package pl.allegro.tech.build.axion.release

class ReleaseBranchesConfiguration {
    private final boolean releaseOnlyOnReleaseBranches
    private final String currentBranch
    private final Set<String> releaseBranchNames

    ReleaseBranchesConfiguration(
        boolean releaseOnlyOnReleaseBranches,
        String currentBranch,
        Set<String> releaseBranchNames
    ) {
        this.releaseOnlyOnReleaseBranches = releaseOnlyOnReleaseBranches
        this.currentBranch = currentBranch
        this.releaseBranchNames = releaseBranchNames
    }

    boolean shouldRelease() {
        return releaseOnlyOnReleaseBranches && !releaseBranchNames.contains(currentBranch)
    }

    String getCurrentBranch() {
        return currentBranch
    }

    Set<String> getReleaseBranchNames() {
        return releaseBranchNames
    }
}
