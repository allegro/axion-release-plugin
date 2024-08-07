package pl.allegro.tech.build.axion.release.domain.scm

class ScmPropertiesBuilder {

    private final File directory

    private String type = 'git'
    private String overriddenBranchName
    private Boolean overriddenIsClean = null
    private Boolean autoDeepenShallowRepo = true

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

    ScmPropertiesBuilder withAutoDeepenShallowRepo(String autoDeepenShallowRepo) {
        this.autoDeepenShallowRepo = autoDeepenShallowRepo
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
                autoDeepenShallowRepo
        )
    }

    ScmPropertiesBuilder ofType(String type) {
        this.type = type
        return this
    }
}
