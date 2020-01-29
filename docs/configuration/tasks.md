# Tasks

`axion-gradle-plugin` adds 8* new Gradle tasks:

-   *verifyRelease*
-   *release*
-   *createRelease*
-   *releaseDependents*
-   *createReleaseDependents*
-   *pushRelease*
-   *currentVersion*
-   *markNextVersion*

*The plugin also creates a helper task called `configureReleaseDependentsTasks` that is called internally, but this is not designed to be called by users.

## verifyRelease

Runs all checks before release. **release** task depends on it. See
[checks](checks.md) for detailed configuration.

## release

Atomically run pre-release actions ([hooks]{role="doc"}), create release
tag and push it to remote. This task is equivalent of running
*createRelease* and *pushRelease* in a single run. The crucial
difference is, *release* task guarantees release & push operations will
be called in single task run without other tasks interrupting. In case
of calling *createRelease* and *pushRelease* via *dependsOn*, it is up
to Gradle to create task execution graph, meaning there is no guarantee
of these tasks running exactly one after another.

## createRelease

Run pre-release actions ([Pre/post release hooks](hooks.md)) and create release tag.

## releaseDependents

Works as per `release`, except that releases are created on any submodules that depend on the project in which this task is called, including transitive dependencies (i.e. changes to `moduleA` will generate releases for `moduleB` and `moduleC`, assuming `moduleC` depends on `moduleB` depends on `moduleA`).  This is described in further detail in [Basic Usage](../basic_usage.md).

## createReleaseDependents

Works as per `createRelease`, except that releases are created on any submodules that depend on the project in which this task is called, including transitive dependencies (i.e. changes to `moduleA` will generate releases for `moduleB` and `moduleC`, assuming `moduleC` depends on `moduleB` depends on `moduleA`).  This is described in further detail in [Basic Usage](../basic_usage.md).

## pushRelease

Push tag to remote.

## currentVersion

Print current version as seen by `axion-gradle-plugin`. It\'s most
convenient to use `cV` abbreviation when running this task often (Gradle
understands camel-case abbreviations).

By default this task outputs:

    Project version: <version>

If plain output is needed, call:

    ./gradlew cV -q -Prelease.quiet
    <version>

## markNextVersion

Create next-version marker tag, that affects current version resolution.
Tag is pushed to remote. See [Next version markers](next_version.md) for details.
