package pl.allegro.tech.build.axion.release.domain.scm

class ScmPropertiesBuilder {

    private final File directory

    private String type = 'git'
    private String overriddenBranchName
    private Boolean overriddenIsClean = null

    private ScmPropertiesBuilder(File directory) {
        this.directory = directory
    }

    static ScmPropertiesBuilder scmProperties(File directory) {
        return new ScmPropertiesBuilder(directory)
    }

    ScmPropertiesBuilder withOverriddenBranchName(String overriddenBranchName) {
        this.overriddenBranchName = overriddenBranchName
        return this
    }

    ScmPropertiesBuilder withOverriddenIsClean(Boolean overriddenIsClean) {
        this.overriddenIsClean = overriddenIsClean
        return this
    }

    ScmProperties build() {
        return new ScmProperties(
                type,
                directory,
                'origin',
                false,
                false,
                false,
                null,
                overriddenBranchName,
                overriddenIsClean,
                ScmIdentity.defaultIdentityWithoutAgents(),
                true
        )
    }

    ScmPropertiesBuilder ofType(String type) {
        this.type = type
        return this
    }
}
