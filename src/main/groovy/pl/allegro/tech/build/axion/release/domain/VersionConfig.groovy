package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.ReleasePlugin
import pl.allegro.tech.build.axion.release.domain.hooks.HooksConfig
import pl.allegro.tech.build.axion.release.domain.properties.Properties
import pl.allegro.tech.build.axion.release.infrastructure.di.Context
import pl.allegro.tech.build.axion.release.infrastructure.di.GradleAwareContext
import pl.allegro.tech.build.axion.release.util.FileLoader

import javax.inject.Inject
import java.util.regex.Pattern

class VersionConfig {

    private final Project project

    boolean localOnly = false

    boolean dryRun

    boolean ignoreUncommittedChanges = true

    boolean useHighestVersion = false

    RepositoryConfig repository

    TagNameSerializationConfig tag = new TagNameSerializationConfig()

    Closure versionCreator = PredefinedVersionCreator.SIMPLE.versionCreator

    Map<String, Object> branchVersionCreator = [:]

    Closure versionIncrementer = PredefinedVersionIncrementer.versionIncrementerFor('incrementPatch')

    Map<String, Object> branchVersionIncrementer = [:]

    Pattern releaseBranchPattern = Pattern.compile('^release(/.*)?$')

    ChecksConfig checks = new ChecksConfig()

    boolean sanitizeVersion = true

    boolean createReleaseCommit = false

    Closure releaseCommitMessage = PredefinedReleaseCommitMessageCreator.DEFAULT.commitMessageCreator

    NextVersionConfig nextVersion = new NextVersionConfig()

    HooksConfig hooks = new HooksConfig()

    private Context context

    private VersionService.DecoratedVersion resolvedVersion = null

    @Inject
    VersionConfig(Project project) {
        this.project = project
        FileLoader.setRoot(project.rootDir)

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

    void hooks(Closure c) {
        project.configure(hooks, c)
    }

    void versionCreator(String type) {
        this.versionCreator = PredefinedVersionCreator.versionCreatorFor(type)
    }

    @Deprecated
    void releaseCommitMessage(Closure c) {
        releaseCommitMessage = c
    }

    @Deprecated
    void createReleaseCommit(boolean createReleaseCommit) {
        this.createReleaseCommit = createReleaseCommit
    }

    void versionCreator(Closure c) {
        this.versionCreator = c
    }

    void branchVersionCreator(Map<String, Object> creators) {
        this.branchVersionCreator = creators
    }

    void branchVersionCreators(Map<String, Object> creators) {
        this.branchVersionCreator = creators
    }

    void versionIncrementer(String ruleName) {
        this.versionIncrementer = PredefinedVersionIncrementer.versionIncrementerFor(ruleName)
    }

    void versionIncrementer(String ruleName, Map configuration) {
        this.versionIncrementer = PredefinedVersionIncrementer.versionIncrementerFor(ruleName, configuration)
    }

    void versionIncrementer(Closure c) {
        this.versionIncrementer = c
    }

    void branchVersionIncrementer(Map<String, Object> creators) {
        this.branchVersionIncrementer = creators
    }

    String getVersion() {
        ensureVersionExists()
        return resolvedVersion.decoratedVersion
    }

    String getUndecoratedVersion() {
        ensureVersionExists()
        return resolvedVersion.undecoratedVersion
    }

    VersionScmPosition getScmPosition() {
        ensureVersionExists()
        return new VersionScmPosition(
            resolvedVersion.position.revision,
            resolvedVersion.position.shortRevision,
            resolvedVersion.position.branch
        )
    }

    private void ensureVersionExists() {
        if (resolvedVersion == null) {
            resolvedVersion = getUncachedVersion()
        }
    }

    VersionService.DecoratedVersion getUncachedVersion() {
        ensureContextExists()
        Properties rules = context.rules()
        return context.versionService().currentDecoratedVersion(rules.version, rules.tag, rules.nextVersion)
    }

    VersionService getVersionService() {
        ensureContextExists()
        return context.versionService()
    }

    private void ensureContextExists() {
        if (context == null) {
            this.context = GradleAwareContext.create(project, this)
        }
    }
}
