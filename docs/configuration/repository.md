# Repository

`axion-release-plugin` has an abstraction layer to support multiple
repositories, although for the time being only **git** is supported.

By default `axion-release` searches for repository in root project
directory. You can change this using `scmVersion.repository.directory`
property:

    scmVersion {
        repository {
            directory.set(project.rootProject.file('../'))
        }
    }

You can also change remote used to push changes:

    scmVersion {
        repository {
            remote.set("myRemote")
        }
    }

By default, all changes are pushed to `origin`.

In some cases (i.e. CI environments) the repository will be working in a
`detached head` state, where a single commit is checked out without
tracking a branch. In these cases, the release task will fail when it
attempts to push local commits to the remote repository. To solve this
error, you can tell the plugin to only push tags to the remote, which is
allowed even when a local branch is not checked out:

    scmVersion {
        repository {
            pushTagsOnly.set(true)
        }
    }

A command line flag, `release.pushTagsOnly` is also available in case
you do not want to set this in your build script:

    ./gradlew release -Prelease.pushTagsOnly

The command line flag **will** override the build script even if the
script has explicitly set the `pushTagsOnly` flag to false.

## Repository backend

By default `axion-release` reads the repository with **JGit**. On very
large repositories the tag walk and the working tree status scan
performed during configuration can take seconds. In that case you can
switch reads to the native `git` executable:

    scmVersion {
        repository {
            backend.set("nativeGit")
        }
    }

A command line flag, `release.scmBackend`, is also available:

    ./gradlew currentVersion -Prelease.scmBackend=nativeGit

Only reads use the `git` executable. Tagging, committing, fetching and
pushing still go through JGit, so authorization keeps working exactly
the same way - see [authorization](authorization.md).

The native backend requires `git` to be available on `PATH`.

See [authorization](authorization.md) for authorization options.
