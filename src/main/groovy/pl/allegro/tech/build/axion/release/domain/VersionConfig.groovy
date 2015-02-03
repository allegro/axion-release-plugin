package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.ReleasePlugin
import pl.allegro.tech.build.axion.release.domain.hooks.HooksConfig
import pl.allegro.tech.build.axion.release.infrastructure.di.Context

import javax.inject.Inject

class VersionConfig {

    private final Project project

    boolean localOnly = false

    boolean dryRun

    RepositoryConfig repository

    TagNameSerializationRules tag = new TagNameSerializationRules()

    Closure versionCreator = PredefinedVersionCreator.DEFAULT.versionCreator

    Map<String, Closure> branchVersionCreators

    ChecksConfig checks = new ChecksConfig()

    boolean sanitizeVersion = true

    boolean createReleaseCommit = false

    Closure releaseCommitMessage = PredefinedReleaseCommitMessageCreator.DEFAULT.commitMessageCreator

    NextVersionConfig nextVersion = new NextVersionConfig()

    HooksConfig hooks = new HooksConfig()
    
    VersionService versionService

    private String resolvedVersion = null

    private VersionWithPosition rawVersion = null

    @Inject
    VersionConfig(Project project) {
        this.project = project

        this.repository = RepositoryConfigFactory.create(project)
        this.dryRun = project.hasProperty(ReleasePlugin.DRY_RUN_FLAG)
    }

    void repository(Closure c) {
        project.configure(repository, c)
    }

    void tag(Closure c) {
        project.configure(tag, c)
    }

    void checks(Closure c) {
        project.configure(checks, c)
    }

    void nextVersion(Closure c) {
        project.configure(nextVersion, c)
    }

    void versionCreator(String type) {
        this.versionCreator = PredefinedVersionCreator.versionCreatorFor(type)
    }

    void releaseCommitMessage(Closure c) {
        releaseCommitMessage = c
    }

    void createReleaseCommit(boolean createReleaseCommit) {
        this.createReleaseCommit = createReleaseCommit
    }

    void versionCreator(Closure c) {
        this.versionCreator = c
    }

    String getVersion() {
        if (resolvedVersion == null) {
            ensureVersionServiceExists()
            resolvedVersion = versionService.currentDecoratedVersion(this, VersionReadOptions.fromProject(project))
        }
        return resolvedVersion
    }

    VersionWithPosition getRawVersion() {
        if (rawVersion == null) {
            ensureVersionServiceExists()
            rawVersion = versionService.currentVersion(this, VersionReadOptions.fromProject(project))
        }
        return rawVersion
    }

    private void ensureVersionServiceExists() {
        this.versionService = Context.instance(project).versionService()
    }
}
