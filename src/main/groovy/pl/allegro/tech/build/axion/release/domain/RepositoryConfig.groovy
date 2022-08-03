package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional

import javax.inject.Inject

abstract class RepositoryConfig extends BaseExtension {

    private static final String CUSTOM_KEY_PROPERTY = 'release.customKey'
    private static final String CUSTOM_KEY_FILE_PROPERTY = 'release.customKeyFile'
    private static final String CUSTOM_KEY_PASSWORD_PROPERTY = 'release.customKeyPassword'
    private static final String USERNAME_PROPERTY = 'release.customUsername'
    private static final String PASSWORD_PROPERTY = 'release.customPassword'
    private static final String RELEASE_PUSH_TAGS_ONLY_PROPERTY = 'release.pushTagsOnly'
    private static final String ATTACH_REMOTE_PROPERTY = 'release.attachRemote'
    private static final String OVERRIDDEN_BRANCH_NAME = 'release.overriddenBranchName'
    private static final String DISABLE_SSH_AGENT = 'release.disableSshAgent'
    private static final String FETCH_TAGS_PROPERTY = 'release.fetchTags'


    @Inject
    RepositoryConfig(Directory repositoryDirectory) {
        pushTagsOnly.convention(false)
        type.convention("git")
        remote.convention("origin")

        customUsername.convention(gradleProperty(USERNAME_PROPERTY))
        customPassword.convention(gradleProperty(PASSWORD_PROPERTY))
        customKeyPassword.convention(gradleProperty(CUSTOM_KEY_PASSWORD_PROPERTY))
        customKey.convention(gradleProperty(CUSTOM_KEY_PROPERTY))
        customKeyFile.convention(gradleProperty(CUSTOM_KEY_FILE_PROPERTY)
            .map({ layout.projectDirectory.file(it) }))
        directory.convention(repositoryDirectory)
    }

    @Input
    @Optional
    abstract Property<String> getType()

    @InputDirectory
    @Optional
    abstract DirectoryProperty getDirectory()

    @Input
    @Optional
    abstract Property<String> getRemote()

    @Input
    @Optional
    abstract Property<String> getCustomKey()

    @Input
    @Optional
    abstract RegularFileProperty getCustomKeyFile()

    @Input
    @Optional
    abstract Property<String> getCustomKeyPassword()

    @Input
    @Optional
    abstract Property<String> getCustomUsername()

    @Input
    @Optional
    abstract Property<String> getCustomPassword()

    @Input
    @Optional
    abstract Property<Boolean> getPushTagsOnly()

    Provider<Boolean> pushTagsOnly() {
        gradlePropertyPresent(RELEASE_PUSH_TAGS_ONLY_PROPERTY).orElse(pushTagsOnly)
    }

    Provider<String> attachRemote() {
        gradleProperty(ATTACH_REMOTE_PROPERTY)
    }

    Provider<String> overriddenBranch() {
        gradleProperty(OVERRIDDEN_BRANCH_NAME)
    }

    Provider<Boolean> disableSshAgent() {
        gradlePropertyPresent(DISABLE_SSH_AGENT).orElse(false)
    }

    Provider<Boolean> fetchTags() {
        gradlePropertyPresent(FETCH_TAGS_PROPERTY).orElse(false)
    }
}
