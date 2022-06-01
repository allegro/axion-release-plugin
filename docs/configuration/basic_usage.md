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
        prefix.set("my-project-name")
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

-   keep in mind, that `scmVersion` must be initialized before
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

**IMPORTANT:**

Note that if the version separator appears in the prefix then tag parsing
will fail. For example, the two prefixes below will not work if the version
separator is `-`:

```
my-service
my-service-client
```

Use the `exclude()` configuration parameter within a `monorepos` block to identify submodules
that should be excluded from consideration when calculating whether to increment
the version of the parent project.  Typically, you would do this in the top level
project, assuming that submodules are named after the directory they appear in:

```
scmVersion {
    monorepos {
        exclude(project.subprojects.collect({p -> p.name}))
    }
}
```

Version calculation rules:
1. Changes to files within a submodule increment that submodule's version only.
2. Changes to a submodule do not cause a change to the parent project's version if
the parent is set to ignore that submodule, via `exclude()`.
3. Changes to files in the parent project but which are not in a submodule identified via
`exclude()` will cause the parent project's version to increment but not the
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

## Accessing previous version

Property `scmVersion.previousVersion` contains the previous version String.
This property is never null.
If there are no previous tags `previousVersion` will be the same as undecorated current version.
This property is useful for scenarios like automated changelog generation between previous and current version
([#138](https://github.com/allegro/axion-release-plugin/issues/138)).
Example usage:

```
tasks.named("myChangelogGenerator") {
    previousRevision = "release-" + scmVersion.previousVersion
    // ...
}
```
