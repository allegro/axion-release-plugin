package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import pl.allegro.tech.build.axion.release.ReleasePlugin
import pl.allegro.tech.build.axion.release.domain.hooks.HooksConfig
import pl.allegro.tech.build.axion.release.domain.properties.Properties
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties
import pl.allegro.tech.build.axion.release.infrastructure.di.Context
import pl.allegro.tech.build.axion.release.infrastructure.di.GradleAwareContext
import pl.allegro.tech.build.axion.release.util.FileLoader

import javax.inject.Inject
import java.util.regex.Pattern

import static pl.allegro.tech.build.axion.release.TagPrefixConf.*

class VersionConfig {

    private final Project project

    @Input
    boolean localOnly = false

    @Input
    boolean dryRun

    @Input
    boolean ignoreUncommittedChanges = true

    @Input
    boolean useHighestVersion = false

    @Nested
    RepositoryConfig repository

    @Nested
    TagNameSerializationConfig tag = new TagNameSerializationConfig()

    @Nested
    VersionProperties.Creator versionCreator = PredefinedVersionCreator.SIMPLE.versionCreator

    @Nested
    VersionProperties.Creator snapshotCreator = PredefinedSnapshotCreator.SIMPLE.snapshotCreator

    @Input
    Map<String, Object> branchVersionCreator = [:]

    @Nested
    VersionProperties.Incrementer versionIncrementer = { VersionIncrementerContext context -> return context.currentVersion.incrementPatchVersion() }

    @Input
    Map<String, Object> branchVersionIncrementer = [:]

    @Input
    Pattern releaseBranchPattern = Pattern.compile('^'+ prefix() + '(/.*)?$')

    @Nested
    ChecksConfig checks = new ChecksConfig()

    @Input
    boolean sanitizeVersion = true

    @Input
    boolean createReleaseCommit = false

    @Nested
    PredefinedReleaseCommitMessageCreator.CommitMessageCreator releaseCommitMessage = PredefinedReleaseCommitMessageCreator.DEFAULT.commitMessageCreator

    @Nested
    NextVersionConfig nextVersion = new NextVersionConfig()

    @Nested
    HooksConfig hooks = new HooksConfig()

    @Nested
    MonorepoConfig monorepoConfig = new MonorepoConfig()

    private Context context

    private VersionService.DecoratedVersion resolvedVersion = null

    @Inject
    VersionConfig(Project project) {
        this.project = project
        FileLoader.setRoot(project.rootDir)

        this.repository = RepositoryConfigFactory.create(project)
        this.dryRun = project.hasProperty(ReleasePlugin.DRY_RUN_FLAG)
    }

    void repository(Action<RepositoryConfig> action) {
        action.execute(repository)
    }

    void tag(Action<TagNameSerializationConfig> action) {
        action.execute(tag)
    }

    void checks(Action<ChecksConfig> action) {
        action.execute(checks)
    }

    void nextVersion(Action<NextVersionConfig> action) {
        action.execute(nextVersion)
    }

    void hooks(Action<HooksConfig> action) {
        action.execute(hooks)
    }

    void versionCreator(String type) {
        this.versionCreator = PredefinedVersionCreator.versionCreatorFor(type)
    }

    @Deprecated
    void releaseCommitMessage(PredefinedReleaseCommitMessageCreator.CommitMessageCreator commitMessageCreator) {
        this.releaseCommitMessage = commitMessageCreator
    }

    @Deprecated
    void createReleaseCommit(boolean createReleaseCommit) {
        this.createReleaseCommit = createReleaseCommit
    }

    void versionCreator(VersionProperties.Creator versionCreator) {
        this.versionCreator = versionCreator
    }

    void snapshotCreator(VersionProperties.Creator snapshotCreator) {
        this.snapshotCreator = snapshotCreator
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

    void versionIncrementer(VersionProperties.Incrementer versionIncrementer) {
        this.versionIncrementer = versionIncrementer
    }

    void branchVersionIncrementer(Map<String, Object> creators) {
        this.branchVersionIncrementer = creators
    }

    @Input
    String getVersion() {
        ensureVersionExists()
        return resolvedVersion.decoratedVersion
    }

    @Input
    String getPreviousVersion() {
        ensureVersionExists()
        return resolvedVersion.previousVersion
    }

    @Input
    String getUndecoratedVersion() {
        ensureVersionExists()
        return resolvedVersion.undecoratedVersion
    }

    @Nested
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

    /**
     * @deprecated Due to the fact it uses the cached context, which results in returning the same
     * version even though project properties got changed. Use {@link #getUncached()} instead.
     * @return uncached version, but based on a cached context
     */
    @Deprecated
    @Nested
    VersionService.DecoratedVersion getUncachedVersion() {
        ensureContextExists()
        return getVersionFromContext(context)
    }

    /**
     * Allows to calculate and get the version, omitting caching mechanisms.
     * May be slower for large projects, use then {@link #getVersion()} instead.
     * @since 1.13.4
     * @return uncached version
     */
    @Nested
    VersionService.DecoratedVersion getUncached() {
        def context = GradleAwareContext.create(project, this)
        return getVersionFromContext(context)
    }

    private static VersionService.DecoratedVersion getVersionFromContext(Context context) {
        Properties rules = context.rules()
        def versionService = context.versionService()
        return versionService.currentDecoratedVersion(rules.version, rules.tag, rules.nextVersion)
    }

    @Nested
    VersionService getVersionService() {
        ensureContextExists()
        return context.versionService()
    }

    private void ensureContextExists() {
        if (context == null) {
            this.context = GradleAwareContext.create(project, this)
        }
    }

    void monorepos(Action<MonorepoConfig> action) {
        action.execute(monorepoConfig)
    }
}
