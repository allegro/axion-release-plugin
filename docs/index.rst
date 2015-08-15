axion-release-plugin
====================

Gradle release and version management plugin documentation.
See `Github project <http://github.com/allegro/axion-release-plugin>`_ to see the source code and motivation for creating
this plugin.

General
-------

.. toctree::
    :titlesonly:
    
    configuration/basic_usage
    configuration/overview
    configuration/tasks

Configuration
-------------

.. toctree::
    :titlesonly:
    
    configuration/authorization
    configuration/version
    configuration/force_version
    configuration/uncommitted_changes
    configuration/next_version
    configuration/checks
    configuration/dry_run
    configuration/repository
    configuration/hooks
    configuration/publishing
    configuration/ci_servers

Examples
--------

.. toctree::
    :maxdepth: 2

    examples/examples.rst

Basic workflow
--------------

Basic workflow with ``axion-release``::

    # git tag
    project-0.1.0

    # ./gradlew currentVersion
    0.1.0

    # git commit -m "Some commit."

    # ./gradlew currentVersion
    0.1.1-SNAPSHOT

    # ./gradlew release

    # git tag
    project-0.1.0 project-0.1.1

    # ./gradlew currentVersion
    0.1.1

    # ./graldew publish
    published project-0.1.1 release version

    # ./gradlew markNextVersion -Prelease.nextVersion=1.0.0

    # ./gradlew currentVersion
    1.0.0-SNAPSHOT

Changelog
=========

.. toctree::
    :hidden:
    
    changelog.rst

Project changelog: :doc:`changelog`.