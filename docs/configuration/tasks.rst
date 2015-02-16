Tasks
=====

``axion-gradle-plugin`` adds 4 new Gradle tasks:

* *verifyRelease*
* *release*
* *currentVersion*
* *markNextVersion*

verifyRelease
-------------

Runs all checks before release. **release** task depends on it. See :doc:`checks` for detailed configuration.

release
-------

Run pre-release actions (:doc:`hooks`), create release tag and push it to remote.

currentVersion
--------------

Print current version as seen by ``axion-gradle-plugin``. It's most convenient to use ``cV`` abbreviation when running
this task often (Gradle understands camel-case abbreviations).

markNextVersion
---------------

Create next-version marker tag, that affects current version resolution. Tag is pushed to remote. See :doc:`next_version`
for details.