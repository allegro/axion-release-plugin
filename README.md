axion-release-plugin
====

*gradle release and version management plugin*

[![Build Status](https://github.com/allegro/axion-release-plugin/actions/workflows/ci.yml/badge.svg)](https://github.com/allegro/axion-release-plugin/actions/workflows/ci.yml)
[![readthedocs](https://readthedocs.org/projects/axion-release-plugin/badge/?version=latest) ](http://axion-release-plugin.readthedocs.org/en/latest/)
![Maven Central](https://img.shields.io/maven-central/v/pl.allegro.tech.build/axion-release-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/pl.allegro.tech.build.axion-release?versionPrefix=1.1)](https://plugins.gradle.org/plugin/pl.allegro.tech.build.axion-release)

Releasing versions in Gradle is very different from releasing in Maven. Maven came with
[maven-release-plugin](http://maven.apache.org/maven-release/maven-release-plugin/) which
did all the dirty work. Gradle has no such tool and probably doesn't need it anyway. Evolution of software craft came
to the point, when we start thinking about SCM as ultimate source of truth about project version. Version should not be
hardcoded in **pom.xml** or **build.gradle**.

**axion-release-plugin** embraces this philosophy. Instead of reading project version from buildfile, it is derived
from nearest tag in SCM (or set to default if nothing was tagged). If current commit is tagged commit, project has
a release version. If there were any commits after last tag, project is in SNAPSHOT version. This very simple and
intuitive philosophy, alongside with [Semantic Versioning](http://semver.org/) rules, makes it a lot easier to manage
project versions along SCM tag versions.

JDK11+ & Gradle 7+ required.

## Basic usage

```kotlin
plugins {
    id("pl.allegro.tech.build.axion-release") version "1.18.7"
}

version = scmVersion.version
```

```
$ git tag
<empty list>

$ ./gradlew currentVersion
0.1.0-branch-SNAPSHOT

$ ./gradlew release

$ git tag
v0.1.0

$ ./gradlew cV
0.1.0

$ git add -A && git commit -m "Updates something" && ./gradlew release

$ git tag
v0.1.0
v0.1.1

$ ./gradlew cV
0.1.1
```

## Documentation

Documentation is available at [axion-release read the docs](https://readthedocs.org/docs/axion-release-plugin/en/latest).

## License

**axion-release-plugin** is published under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
