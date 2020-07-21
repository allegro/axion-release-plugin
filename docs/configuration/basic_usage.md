# Basic usage

## Applying plugin

`axion-release-plugin` is published in both [Maven
Central](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22axion-release-plugin%22)
and [Gradle Plugin
Portal](http://plugins.gradle.org/plugin/pl.allegro.tech.build.axion-release).

### Gradle 2.1+

```
buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    id 'pl.allegro.tech.build.axion-release' version ''
}
```

Maven Central needs to be imported in buildscript, as there are some
dependencies not published in *jCenter* repository.

### Maven Central

```
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath group: 'pl.allegro.tech.build', name: 'axion-release-plugin', version: ''
    }
}

apply plugin: 'pl.allegro.tech.build.axion-release'
```

## Basic configuration

### Single module project

```
scmVersion {
    tag {
        prefix = 'my-project-name'
    }
}

project.version = scmVersion.version
```

> Order of definition does matter! First, you need to apply the plugin,
> then configure it using `scmVersion` extension and only then current
> version can be set for whole project via `scmVersion.version`.

### Multi-module project

For multi project builds the plugin has to be applied only on the root
project, but version has to be set also in submodules

```
plugins {
    id 'pl.allegro.tech.build.axion-release' version ''
}

scmVersion {
    // ...
}

allprojects {
    project.version = scmVersion.version
}
```

### Multi-module project with multiple versions

Sometimes it might be desirable to release each module (or just some
modules) of multi-module project separately. If so, please make sure
that:

-   you keep in mind that `scmVersion` must be initialized before
    `scmVersion.version` is accessed
-   apply plugin on each module that should be released on its own

The plugin ignores tags based on the prefix and version separator
(i.e. `<prefix><separator>`).  If there is a prefix configured then
tags that do not start with the configured `<prefix><separator>` are 
all ignored. If there is no prefix configured then only tags matching
the version are used for calculating the version (i.e. the version 
separator is also ignored).

This allows for using the name of the module with an appropriate version
separator as a namespace in a multi-module project, as shown in the table
below:

| Module name | Version separator | Tags will appear as |
|-------------|-------------------|---------------------|
| `module`    | `-`               | `module-<maj>.<min>.<patch>` |
| `moduleV2`    | `-`               | `moduleV2-<maj>.<min>.<patch>` |

For example, within `module`, tags that do not start `module-` will be
ignored. 

Thus we can configure each subproject within such a multimodule project like this:

```
scmVersion {
    tag {
        prefix = "${project.name}"
    }
}
```

**IMPORTANT:**  

Note that if the version separator appears in the prefix then tag parsing
will fail. For example, the two prefixes below will not work if the version
separator is `-`:

```
my-service
my-service-client
```

Use the `projectDirs` configuration parameter within a `monorepos` block to identify submodules
that should be excluded from consideration when calculating whether to increment
the version of the parent project.  Typically, you would do this in the top level
project, assuming that submodules are named after the directory they appear in:

```
scmVersion {
    monorepos {
        projectDirs = project.subprojects.collect({p -> p.name})
    }
}
```

Version calculation rules:
1. Changes to files within a submodule increment that submodule's version only.
2. Changes to a submodule do not cause a change to the parent project's version if
the parent is set to ignore that submodule, via `projectDirs`.
3. Changes to files in the parent project but which are not in a submodule identified via 
`projectDirs` will cause the parent project's version to increment but not the 
versions of any submodules.  If this is desired then consider wiring the `createRelease` or
`release` tasks of the submodules to be dependencies of the tasks of the same name in the parent. 

## Releasing

```
# ./gradlew currentVersion
0.1.0-SNAPSHOT

# ./gradlew release

# ./gradlew cV
0.1.0
```

For a multimodule project which has project lib dependencies (e.g. `moduleB` `dependsOn` `project(':moduleA')`), a change within `moduleA`'s code will result in the version of `moduleA` being incremented normally.  However, `moduleB`'s code may not have changed and thus it will not have its version incremented, which is usually not the desired behaviour.  This issue is rectified by the 2 tasks, `createReleaseDependents` and `releaseDependents`.  These tasks are analogous to `createRelease` and `release`, respectively, and traverse the inter-project dependency tree and cause the creation of releases for all projects that have a declared dependency on a project(s) whose code has changed.  Thus, for the example just cited, a new release would be created for `moduleB`.  This is modelled on how the Java plugin's `buildNeeded` and `buildDependents` tasks work (described [here](https://docs.gradle.org/current/userguide/multi_project_builds.html#sec:multiproject_build_and_test)).
