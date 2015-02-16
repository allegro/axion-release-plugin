Repository
==========

``axion-release-plugin`` has abstraction layer to support multiple repositories, although for the time being only
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

See :doc:`authorization` for authorization options.