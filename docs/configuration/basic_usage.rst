Basic usage
===========

Applying plugin
---------------

``axion-release-plugin`` is published in both 
`Maven Central <http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22axion-release-plugin%22>`_ and
`Gradle Plugin Portal <http://plugins.gradle.org/plugin/pl.allegro.tech.build.axion-release>`_.

Gradle 2.1+
^^^^^^^^^^^

::

    buildscript {
        repositories {
            mavenCentral()
        }
    }

    plugins {
        id 'pl.allegro.tech.build.axion-release' version '|version|'
    }

Maven Central needs to be imported in buildscript, as there are some dependencies not published in *jCenter* repository.

Maven Central
^^^^^^^^^^^^^

::

    buildscript {
        repositories {
            mavenCentral()
        }
        dependencies {
            classpath group: 'pl.allegro.tech.build', name: 'axion-release-plugin', version: '|version|'
        }
    }

    apply plugin: 'pl.allegro.tech.build.axion-release'


Basic configuration
-------------------

Single module project
^^^^^^^^^^^^^^^^^^^^^

::

    scmVersion {
        tag {
            prefix = 'my-project-name'
        }
    }

    project.version = scmVersion.version

.. note::
    Order of definition does matter! First, you need to apply the plugin, then configure it using ``scmVersion`` extension
    and only then current version can be set for whole project via ``scmVersion.version``.

Multi-module project
^^^^^^^^^^^^^^^^^^^^

For multi project builds the plugin has to be applied only on the root project, but version has to be set also in submodules::

    plugins {
        id 'pl.allegro.tech.build.axion-release' version '|version|'
    }

    allprojects {
        project.version = scmVersion.version
    }
