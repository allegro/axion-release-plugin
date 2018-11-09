package pl.allegro.tech.build.axion.release.domain.scm

class ScmPropertiesBuilder {

    private final File directory

    private String type = 'git'

    private ScmPropertiesBuilder(File directory) {
        this.directory = directory
    }

    static ScmPropertiesBuilder scmProperties(File directory) {
        return new ScmPropertiesBuilder(directory)
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
                ScmIdentity.defaultIdentityWithoutAgents()
        )
    }

    ScmPropertiesBuilder ofType(String type) {
        this.type = type
        return this
    }
}
