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

### Multi-module with multiple versions

Sometimes it might be desirable to release each module (or just some
modules) of multi-module project separately. If so, please make sure
that:

-   tag prefixes for each module do not overlap, i.e.
    `!tagA.startsWith(tagB)` for each permutation of all tag prefixes
-   keep in mind, that `scmVersion` must be initialized before
    `scmVersion.version` is accessed
-   apply plugin on each module that should be released on it\'s own

Use the `foldersToExclude` configuration parameter to identify submodules
that should be excluded from consideration when calculating whether to increment
the version of the parent project.  Typically, you would do this in the top level
project, assuming that submodules are named after the directory they appear in:

```
scmVersion {
    foldersToExclude = project.subprojects.collect({p -> p.name})
}
```

Version calculation rules:
1. Changes to files within a submodule increment that submodule's version only.
2. Changes to a submodule do not cause a change to the parent project's version if
the parent is set to ignore that submodule, via `foldersToExclude`.
3. Changes to files in the parent project but which are not in a submodule identified via 
`foldersToExclude` will cause the parent project's version to increment but not the 
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
