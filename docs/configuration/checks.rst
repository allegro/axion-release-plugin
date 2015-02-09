Pre-release checks
==================

By default ``axion-release-plugin`` runs ``verifyRelease`` task before performing release. You can control which
checks are enabled by default using ``scmVersion.checks`` properties::

    scmVersion {
        checks {
            uncommittedChanges = false
            aheadOfRemote = false
        }
    }

You can also use ``release.disableChecks`` flags to disable all checks for current release build::

    ./gradlew release -Prelease.disableChecks

Uncommited changes check
------------------------

This check seeks staged, but uncommitted changes in repository. It will stop the build if they are found. Since in some
corner cases ``JGit`` behaves different than shell ``git``, this task will print all staged and unstaged changes as seen
by plugin on failure::

    ./gradlew release
    :verifyRelease
    Looking for uncommitted changes.. FAILED

    Staged changes:

    Unstaged changes:
        modified: build.gradle
        modified: README.md

You can disable this check using either ``scmVersion.checks.uncommitedChanges`` property or via
``release.disableUncommittedCheck`` command line option::

    ./gradlew release -Prelease.disableUncommittedCheck

Ahead of remote check
---------------------

This check tries to verify if all local commits have been pushed to remote. In order to do so, it need full information
about remote and HEAD commit, which might be lacking in CI environment (for more on CI build read :doc:`ci_servers`).

You can disable this check using either ``scmVersion.checks.aheadOfRemote`` property or via
``release.disableRemoteCheck`` command line option::

    ./gradlew release -Prelease.disableRemoteCheck