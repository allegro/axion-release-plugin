# Dry run & local only release

In some cases, especially when setting up `axion-release-plugin` or
experimenting with new features it is desirable to either avoid
modification of repository or to persist changes in local repository
only.

## Dry run

In dry-run mode, no changes are made to repository. `axion-release`
still interacts with repository to read things, but all write operations
are mocked and printed out to indicated what *would* happen. To use dry
run mode:

    #./gradlew release -Prelease.dryRun
    :verifyRelease
    DRY-RUN: uncommitted changes: true
    Looking for uncommitted changes.. FAILED
    DRY-RUN: ahead of remote: true
    Checking if branch is ahead of remote.. FAILED
    :release
    Creating tag: v0.8.2
    DRY-RUN: creating tag with name: v0.8.2
    Pushing all to remote: origin
    DRY-RUN: pushing to remote: origin

## Local only

In local only mode any actions that interact with remote are skipped.
Local only mode can be switch on either using command line flag:

    ./gradlew release -Prelease.localOnly

or by altering configuration:

    scmVersion {
        localOnly.set(true)
    }

Flag has precedence over configuration. Local only mode is evaluated
lazily, so it can be changed during build.
