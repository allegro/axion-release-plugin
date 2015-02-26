Tasks
=====

``axion-gradle-plugin`` adds 6 new Gradle tasks:

* *verifyRelease*
* *release*
* *createRelease*
* *pushRelease*
* *currentVersion*
* *markNextVersion*

verifyRelease
-------------

Runs all checks before release. **release** task depends on it. See :doc:`checks` for detailed configuration.

release
-------

Atomically run pre-release actions (:doc:`hooks`), create release tag and push it to remote. This task is equivalent
of running *createRelease* and *pushRelease* in a single run. The crucial difference is,
*release* task guarantees release & push operations will be called in single task run without other tasks interrupting.
In case of calling *createRelease* and *pushRelease* vai *dependsOn*, it is up to Gradle to create task execution
graph, meaning there is no guarantee of these tasks running exactly one after another.

createRelease
-------------

Run pre-release actions (:doc:`hooks`) and create release tag.

pushRelease
-----------

Push tag to remote.

currentVersion
--------------

Print current version as seen by ``axion-gradle-plugin``. It's most convenient to use ``cV`` abbreviation when running
this task often (Gradle understands camel-case abbreviations).

markNextVersion
---------------

Create next-version marker tag, that affects current version resolution. Tag is pushed to remote. See :doc:`next_version`
for details.