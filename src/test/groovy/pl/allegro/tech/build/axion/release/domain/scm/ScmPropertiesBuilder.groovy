package pl.allegro.tech.build.axion.release.domain.scm

import org.gradle.api.provider.ProviderFactory
import org.gradle.testfixtures.ProjectBuilder

class ScmPropertiesBuilder {

    // ProviderFactory is only obtainable from a Gradle project, one throwaway project per JVM is enough
    private static final ProviderFactory PROVIDERS = ProjectBuilder.builder().build().providers

    private final File directory

    private String type = 'git'
    private String overriddenBranchName
    private Boolean overriddenIsClean = null
    private ScmBackend backend = ScmBackend.JGIT

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

    ScmPropertiesBuilder withBackend(ScmBackend backend) {
        this.backend = backend
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
            true,
            ['main', 'master'] as Set,
            false,
            true,
            'v',
            backend,
            PROVIDERS
        )
    }

    ScmPropertiesBuilder ofType(String type) {
        this.type = type
        return this
    }
}
