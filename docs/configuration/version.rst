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

Git implementation of repository interface starts from current commit and goes up the commit tree until first tag
with matching prefix is encountered. This tag is returned as current position in repository (along with current branch)
and commit number. Information on what prefix to match can be set using ``scmVersion.tag.prefix`` property::

    scmVersion {
        tag {
            prefix = 'my-prefix'
        }
    }

By default it equals ``release``.

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

``position`` object contains:

* ``latestTag`` - the name of the latest tag
* ``branch`` - the name of the current branch
* ``onTag`` - true, if current commit is tagged with release version tag

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

Decorating
----------

Decorating phase happens only when version is read (and deserialized). During this phase version can be decorated with
i.e. branch name. Default decorator does nothing. ``axion-release-plugin`` supports adding predefined named version creators
(so don't be afraid to post pull request if you have something useful!). Decoration phase is conducted by *version creators*,
you can configure it via ``scmVersion.versionCreator`` method::

    scmVersion {
        versionCreator 'versionWithBranch'
    }

You can also set decorators per branches that match specific regular expression::

    scmVersion {
        branchVersionCreators = [
            'feature/.*': { version, position -> ...},
            'bugfix/.*': { version, position -> ...}
        ]
    }

Per-branch version creators must be closures, there is no support for predefined creators.

versionWithBranch
^^^^^^^^^^^^^^^^^

::

    scmVersion {
        versionCreator 'versionWithBranch'
    }

This version creator appends branch name to version unless you are on *master*::

    decorate(version: '0.1.0', branch: 'master') == 0.1.0
    decorate(version: '0.1.0', branch: 'my-special-branch') == 0.1.0-my-special-branch

Custom version creator
^^^^^^^^^^^^^^^^^^^^^^

Custom version creators can be implemented by creating closure::

    {version, position -> ...}
    
* version - string version resolved by previous steps
* position - object described above in *Serialization* section

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
