# Pre-release checks

By default `axion-release-plugin` runs `verifyRelease` task before
performing release. You can control which checks are enabled by default
using `scmVersion.checks` properties:

    scmVersion {
        checks {
            uncommittedChanges.set(false)
            aheadOfRemote.set(false)
            snapshotDependencies.set(false)
        }
    }

You can also use `release.disableChecks` flags to disable all checks for
current release build:

    ./gradlew release -Prelease.disableChecks

## Uncommitted changes check

This check seeks staged, but uncommitted changes in repository. It will
stop the build if they are found. Since in some corner cases `JGit`
behaves different from shell `git`, this task will print all staged and
unstaged changes as seen by plugin on failure:

    ./gradlew release
    :verifyRelease
    Looking for uncommitted changes.. FAILED

    Staged changes:

    Unstaged changes:
        modified: build.gradle
        modified: README.md

You can disable this check using either
`scmVersion.checks.uncommitedChanges` property or via
`release.disableUncommittedCheck` command line option:

    ./gradlew release -Prelease.disableUncommittedCheck

## Ahead of remote check

This check verifies that:

- all local commits have been pushed to remote
- no new remote commits appeared that haven't been pulled to local

For long-running CI builds, it may happen that build for older merge commit will finish after the newer merge commit.
In this case, the versions would be mixed (older commit would be tagged with a newer version).

It needs full information about remote and
HEAD commit, which might be lacking in CI environment (for more on CI
build read [CI Servers](ci_servers.md)).

You can disable this check using either
`scmVersion.checks.aheadOfRemote` property or via
`release.disableRemoteCheck` command line option:

    ./gradlew release -Prelease.disableRemoteCheck

## Snapshot dependencies check

This check performs validation that the build has no snapshot libraries
in dependencies.

You can disable this check using either
`scmVersion.checks.snapshotDependencies` property or via
`release.disableSnapshotsCheck` command line option:

    ./gradlew release -Prelease.disableSnapshotsCheck
