Version creation
================

``axion-release-plugin`` comes with rich set of features for version parsing and decorating. Extracting version
information from repository can be split in six phases:

* reading - read tags from repository
* parsing - extracting version from tag (serializing version to tag)
* incrementing - when not on tag, version patch (leas significant) number is incremented
* decorating - adding additional transformations to create final version
* appending snapshot - when not on tag, SNAPSHOT suffix is appended
* sanitization - making sure there are no unwanted characters in version

Reading
-------

Version is kept in repository in form of a tag::

    # git tag
    release-1.0.0 release-1.0.1 release-1.1.0

Only tags which math predefined prefix are taken into a count when calculating
current version. Prefix can be set using ``scmVersion.tag.prefix`` property::

    scmVersion {
        tag {
            prefix = 'my-prefix'
        }
    }

Default prefix is ``release``.

There is also an option to set prefix per-branch (i.e. to use different version prefix on ``legacy-`` branches)::

    scmVersion {
        tag {
            prefix = 'default-prefix'
            branchPrefix = [
                'legacy.*' : 'legacy-prefix'
            ]
        }
    }

Git implementation of a repository interface can operate in two modes when
searching for tag to read the version from.

First tag encountered
^^^^^^^^^^^^^^^^^^^^^

**This is the default mode**

In default mode, search for tag starts from current commit and goes up the commit tree until first tag
with matching prefix is encountered. This tag is returned as current position in repository (along with current branch)
and commit number. 

Tree walking algorithm might lead to various misunderstandings. Take this tree as an example::

        [T1]
         |
      ------
     |      |
    [_]    [T2]
     |      |
    [C]    [_]

Let ``T*`` be a tagged commit and ``C`` current commit. After releasing version with tag ``T1``, we have been working
on two separate branches. Then, branch on the right has been released and marked with ``T2`` tag. When traversing this
tree from commit ``C`` upwards, first discovered tag will be ``T1`` and so reported version will come from parsing
``T1`` tag, even though tag ``T2`` has higher version number. It all comes down to what is the history of current commit,
not what has happened in repository in general.

.. _use_highest_version:

Tag with highest version
^^^^^^^^^^^^^^^^^^^^^^^^

Second mode is searching for highest version visible in the git tree's history.
This means that all commits from HEAD till first commit will be analysed.

In order to activate this feature::

    scmVersion {
        useHighestVersion = true
    }

With a tree similar to this::

    Tag: 1.0.0
    Tag: 1.5.0
    Tag: 1.2.0

This changes behavior from::

    # ./gradlew currentVersion
    1.2.0

to::

    # ./gradlew currentVersion
    1.5.0

You can also active this option using command line::

    # ./gradlew currentVersion -Prelease.useHighestVersion
    1.5.0

.. _version-parsing:

Parsing
-------

Having current tag name, we can deserialize it to extract raw version. Actually, this has to be a two-way process, since
on reading version we deserialize and serialize on creating new tag to mark new release.

Deserialization
^^^^^^^^^^^^^^^

Default deserialization function is a simple closure that strips tag name off prefix and separator between prefix and version.
It ignores separator when prefix is an empty string::

    deserialize(prefix: 'release', separator: '-', tag: 'release-1.0.0') == 1.0.0
    deserialize(prefix: '', separator: '-', tag: '1.0.0') == 1.0.0

You can implement own deserializer by setting closure that would accept deserialization config object and position in SCM::

    scmVersion {
        tag {
            deserialize = {config, position, tagName -> ...}
        }
    }

``config`` object is instance of ``TagNameSerializationRules`` class. Useful properties are:

* ``prefix``: tag prefix
* ``separator``: separator between prefix and version

.. _scm-position:

``position`` object contains:

* ``branch`` - the name of the current branch

Last but not least, ``tagName`` contains prepared tag name that should be used to extract version. ``position.latestTag``
might point to next version tag with additional suffix.

Serialization
^^^^^^^^^^^^^

Default serializer prepends prefix and separator to version number. Separator is ignored if prefix is an empty string::

    serialize(prefix: 'release', separator: '-', version: '1.0.0') == release-1.0.0
    serialize(prefix: '', separator: '-', version: '1.0.0') == 1.0.0

You can implement own serializer by setting closure that would accept serialization config object and version::

    scmVersion {
        tag {
            serialize = {config, version -> ...}
        }
    }

``config`` object has been described above.

Initial version
^^^^^^^^^^^^^^^

When starting work on new project there are no tags available and so there is no way to deserialize version. By default
``0.1.0`` version is returned, but you can override that behavior by specifying own closure that will construct initial
version::

    scmVersion {
        tag {
            initialVersion = {config, position -> ...}
        }
    }

Input objects have same structure as deserialization closure inputs.

.. _version-incrementing:

Incrementing
------------

Incrementing phase does incrementing the version in accordance with *version incrementer*. By default version patch
(least significant) number is incremented. There are other predefined rules:

* *incrementPatch* - increment patch number
* *incrementMinor* - increment minor (middle) number
* *incrementMajor* - increment major number
* *incrementMinorIfNotOnRelease* - increment patch number if on release branch. Increment minor otherwise
* *incrementPrerelease* - increment pre-release suffix if possible (-rc1 to -rc2). Increment patch otherwise

You can set one of predefined rules via ``scmVersion.versionIncrementer`` method::

    scmVersion {
        versionIncrementer 'incrementPatch'
    }

Or via ``release.versionIncrementer`` command line argument, which overrides any other incrementer settings::

    ./gradlew release -Prelease.versionIncrementer=incrementMajor

If rule accepts parameters, they can be passed via configuration map::

    scmVersion {
        versionIncrementer 'someIncrementer', [:]
    }

Alternatively you can specify a custom rule by setting a closure that would accept a context object and return a ``Version`` object::

    scmVersion {
        versionIncrementer { context -> ... }
    }

The context object passed to closure contains the following:

* *currentVersion* - current ``Version`` object that should be used to calculate next version (`Version API <https://github.com/zafarkhaja/jsemver/blob/1f4996ea3dab06193c378fd66fd4f8fdc8334cc6/src/main/java/com/github/zafarkhaja/semver/Version.java>`_)
* *position* - widely used position object, for more see :doc:`scm-position`

You can also specify different incrementers per branch. They can be either closure, name of predefined incrementer or
name and list of arguments in case predefined incrementer requires configuration::

    scmVersion {
        branchVersionIncrementer = [
            'feature/.*' : 'incrementMinor',
            'bugfix/.*' : { c -> c.currentVersion.incrementPatchVersion() },
            'legacy/.*' : [ 'incrementMinorIfNotOnRelease', [releaseBranchPattern: 'legacy/release.*'] ]
        ]
    }

If none matches current branch, incrementer set in ``versionIncrementer`` field is used.

incrementMinorIfNotOnRelease
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

This rule uses additional parameter ``releaseBranchPattern`` (by default it's set to 'release/.+')::

    scmVersion {
        versionIncrementer 'incrementMinorIfNotOnRelease', [releaseBranchPattern: 'release.*']
    }

.. _version-decorating:

Decorating
----------

Decorating phase happens only when version is read (and deserialized). During this phase version can be decorated with
i.e. branch name. Default decorator does nothing. ``axion-release-plugin`` supports adding predefined named version creators
(so don't be afraid to post pull request if you have something useful!). Decoration phase is conducted by *version creators*,
you can configure it via ``scmVersion.versionCreator`` method::

    scmVersion {
        versionCreator 'versionWithBranch'
    }

Or via ``release.versionCreator`` command line argument, which overrides any other versionCreator settings::

    ./gradlew release -Prelease.versionCreator=simple

You can also set decorators per branches that match specific regular expression::

    scmVersion {
        branchVersionCreator = [
            'feature/.*': { version, position -> ...},
            'bugfix/.*': 'simple'
        ]
    }

Per-branch version creators must be closures, there is no support for predefined creators. First match wins, but the order
depends on collection type used (default for ``[:]`` is LinkedHashMap).

simple
^^^^^^^

This is the default version creator that does nothing::

    decorate(version: '0.1.0') == 0.1.0

It might be useful when you want some branches to do *nothing*::

    scmVersion {
        branchVersionCreator = [
            'feature/.*': { version, position -> ...},
            'release/.*': 'simple'
        ]
    }

.. _versionWithBranch:

versionWithBranch
^^^^^^^^^^^^^^^^^

::

    scmVersion {
        versionCreator 'versionWithBranch'
    }

This version creator appends branch name to version unless you are on *master* or *detached HEAD*::

    decorate(version: '0.1.0', branch: 'master') == 0.1.0
    decorate(version: '0.1.0', branch: 'my-special-branch') == 0.1.0-my-special-branch

Custom version creator
^^^^^^^^^^^^^^^^^^^^^^

Custom version creators can be implemented by creating closure::

    {version, position -> ...}
    
* version - string version resolved by previous steps
* position - object described above :doc:`scm-position` section

.. _version-sanitization:

Sanitization
------------

After decorating versions, there might be some characters left in version that are not i.e. filename friendly. That's
why last phase of version creation is sanitizing version string. By all characters that do not match ``[A-Za-z0-9._-]``
group are replaced with `-`. For example::

    sanitize('0.1.0-feature/myfeatureBranch-SNAPSHOT') == '0.1.0-feature-my-feature-branch-SNAPSHOT'

You can switch off version sanitization via ``scmVersion.sanitizeVersion`` property::

    scmVersion {
        sanitizeVersion = false
    }
