package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Action
import org.gradle.api.file.Directory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import pl.allegro.tech.build.axion.release.domain.hooks.HooksConfig
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties
import pl.allegro.tech.build.axion.release.infrastructure.di.MemoizedVersionSupplier
import pl.allegro.tech.build.axion.release.infrastructure.di.VersionSupplier

import javax.inject.Inject
import java.util.regex.Pattern

import static pl.allegro.tech.build.axion.release.TagPrefixConf.defaultPrefix

abstract class VersionConfig extends BaseExtension {
    private final VersionSupplier versionSupplier = new VersionSupplier()
    private final MemoizedVersionSupplier cachedVersionSupplier = new MemoizedVersionSupplier()

    private static final String DRY_RUN_FLAG = 'release.dryRun'
    private static final String IGNORE_UNCOMMITTED_CHANGES_PROPERTY = 'release.ignoreUncommittedChanges'
    private static final String FORCE_SNAPSHOT_PROPERTY = 'release.forceSnapshot'
    private static final String USE_HIGHEST_VERSION_PROPERTY = 'release.useHighestVersion'
    private static final String LOCAL_ONLY = "release.localOnly"
    private static final String FORCE_VERSION_PROPERTY = 'release.version'
    private static final String DEPRECATED_FORCE_VERSION_PROPERTY = 'release.forceVersion'
    private static final String VERSION_INCREMENTER_PROPERTY = 'release.versionIncrementer'
    private static final String VERSION_CREATOR_PROPERTY = 'release.versionCreator'

    @Inject
    VersionConfig(Directory repositoryDirectory) {
        getDryRun().convention(gradlePropertyPresent(DRY_RUN_FLAG).orElse(false))
        getLocalOnly().convention(false)
        getIgnoreUncommittedChanges().convention(true)
        getUseHighestVersion().convention(false)
        getReleaseBranchPattern().convention(Pattern.compile('^' + defaultPrefix() + '(/.*)?$'))
        getSanitizeVersion().convention(true)
        getCreateReleaseCommit().convention(false)
        getVersionCreator().convention(PredefinedVersionCreator.SIMPLE.versionCreator)
        getVersionIncrementer().convention((VersionProperties.Incrementer) { VersionIncrementerContext context -> return context.currentVersion.incrementPatchVersion() })
        getSnapshotCreator().convention(PredefinedSnapshotCreator.SIMPLE.snapshotCreator)
        getReleaseCommitMessage().convention(PredefinedReleaseCommitMessageCreator.DEFAULT.commitMessageCreator)
        repository = objects.newInstance(RepositoryConfig, repositoryDirectory)
    }

    @Nested
    final RepositoryConfig repository

    @Nested
    final TagNameSerializationConfig tag = objects.newInstance(TagNameSerializationConfig)

    @Nested
    final ChecksConfig checks = objects.newInstance(ChecksConfig)

    @Nested
    final NextVersionConfig nextVersion = objects.newInstance(NextVersionConfig)

    @Nested
    final HooksConfig hooks = objects.newInstance(HooksConfig)

    @Nested
    final MonorepoConfig monorepoConfig = objects.newInstance(MonorepoConfig)

    @Input
    @Optional
    abstract Property<Boolean> getLocalOnly()

    @Input
    @Optional
    abstract Property<Boolean> getDryRun()

    @Input
    @Optional
    abstract Property<Boolean> getIgnoreUncommittedChanges()

    @Input
    @Optional
    abstract Property<Boolean> getUseHighestVersion();

    @Input
    @Optional
    abstract MapProperty<String, Object> getBranchVersionIncrementer();

    @Input
    @Optional
    abstract Property<Pattern> getReleaseBranchPattern();

    @Input
    @Optional
    abstract Property<Boolean> getSanitizeVersion()

    @Input
    @Optional
    abstract Property<Boolean> getCreateReleaseCommit()

    @Internal
    abstract Property<VersionProperties.Creator> getVersionCreator()

    @Internal
    abstract Property<VersionProperties.Creator> getSnapshotCreator()

    @Internal
    abstract MapProperty<String, Object> getBranchVersionCreator()

    @Internal
    abstract Property<VersionProperties.Incrementer> getVersionIncrementer()

    @Internal
    abstract Property<PredefinedReleaseCommitMessageCreator.CommitMessageCreator> getReleaseCommitMessage()

    Provider<Boolean> ignoreUncommittedChanges() {
        gradlePropertyPresent(IGNORE_UNCOMMITTED_CHANGES_PROPERTY)
            .orElse(ignoreUncommittedChanges)
    }

    Provider<Boolean> forceSnapshot() {
        gradlePropertyPresent(FORCE_SNAPSHOT_PROPERTY).orElse(false)
    }

    Provider<Boolean> useHighestVersion() {
        gradlePropertyPresent(USE_HIGHEST_VERSION_PROPERTY).orElse(useHighestVersion)
    }

    Provider<Boolean> localOnly() {
        gradlePropertyPresent(LOCAL_ONLY).orElse(localOnly)
    }

    Provider<String> forcedVersion() {
        gradleProperty(FORCE_VERSION_PROPERTY)
            .orElse(gradleProperty(DEPRECATED_FORCE_VERSION_PROPERTY))
        .map({it.trim()})
        .map({ it.isBlank() ? null : it})
    }

    Provider<String> versionIncrementerType() {
        gradleProperty(VERSION_INCREMENTER_PROPERTY)
    }

    Provider<String> versionCreatorType() {
        gradleProperty(VERSION_CREATOR_PROPERTY)
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

    void monorepos(Action<MonorepoConfig> action) {
        action.execute(monorepoConfig)
    }

    void versionCreator(String type) {
        this.versionCreator.set(PredefinedVersionCreator.versionCreatorFor(type))
    }

    @Deprecated
    void releaseCommitMessage(PredefinedReleaseCommitMessageCreator.CommitMessageCreator commitMessageCreator) {
        this.releaseCommitMessage.set(commitMessageCreator)
    }

    @Deprecated
    void createReleaseCommit(boolean createReleaseCommit) {
        this.createReleaseCommit.set(createReleaseCommit)
    }

    void versionCreator(VersionProperties.Creator versionCreator) {
        this.versionCreator.set(versionCreator)
    }

    void snapshotCreator(VersionProperties.Creator snapshotCreator) {
        this.snapshotCreator.set(snapshotCreator)
    }

    void branchVersionCreator(Map<String, Object> creators) {
        this.branchVersionCreator.putAll(creators)
    }

    void branchVersionCreators(Map<String, Object> creators) {
        this.branchVersionCreator.putAll(creators)
    }

    void versionIncrementer(String ruleName) {
        this.versionIncrementer.set(PredefinedVersionIncrementer.versionIncrementerFor(ruleName))
    }

    void versionIncrementer(String ruleName, Map configuration) {
        this.versionIncrementer.set(PredefinedVersionIncrementer.versionIncrementerFor(ruleName, configuration))
    }

    void versionIncrementer(VersionProperties.Incrementer versionIncrementer) {
        this.versionIncrementer.set(versionIncrementer)
    }

    void branchVersionIncrementer(Map<String, Object> creators) {
        this.branchVersionIncrementer.putAll(creators)
    }

    Provider<VersionService.DecoratedVersion> versionProvider() {
        def cachedVersionSupplier = this.cachedVersionSupplier
        providers.provider( { cachedVersionSupplier.resolve(this,layout.projectDirectory)})
    }

    Provider<VersionService.DecoratedVersion> uncachedVersionProvider() {
        def versionSupplier = this.versionSupplier
        providers.provider( { versionSupplier.resolve(this, layout.projectDirectory)})
    }

    @Nested
    VersionService.DecoratedVersion getUncached() {
        return uncachedVersionProvider().get()
    }

    @Input
    String getVersion() {
        return versionProvider().map({ it.decoratedVersion}).get()
    }

    @Input
    String getPreviousVersion() {
        return versionProvider().map({ it.previousVersion}).get()
    }

    @Input
    String getUndecoratedVersion() {
        return versionProvider().map({ it.undecoratedVersion}).get()
    }

    @Nested
    VersionScmPosition getScmPosition() {
        return versionProvider().map({ new VersionScmPosition(
            it.position.revision,
            it.position.shortRevision,
            it.position.branch
        )}).get()
    }
}
