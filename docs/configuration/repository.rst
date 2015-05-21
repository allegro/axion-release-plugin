Repository
==========

``axion-release-plugin`` has an abstraction layer to support multiple repositories, although for the time being only
**git** is supported.

By default ``axion-release`` searches for repository in root project directory. You can change this using
``scmVersion.repository.directory`` property::

    scmVersion {
        repository {
            directory = project.rootProject.file('../')
        }
    }

You can also change remote used to push changes::

    scmVersion {
        repository {
            remote = 'myRemote'
        }
    }

By default all changes are pushed to ``origin``.

In some cases (i.e. CI environments) the repository will be working in a 'detached head' state, where a single commit
is checked out without tracking a branch. In these cases, the release task will fail when it attempts to push local
commits to the remote repository. To solve this error, you can tell the plugin to only push tags to the remote,
which is allowed even when a local branch is not checked out::

    scmVersion {
        repository {
            pushTagsOnly = true
        }
    }

A command line flag, ``repository.pushTagsOnly`` is also available in case you do not want to set this in your
build script::

    ./gradlew release -Prepository.pushTagsOnly

The command line flag **will** override the build script even if the script has explicitly set the ``pushTagsOnly``
flag to false.

See :doc:`authorization` for authorization options.