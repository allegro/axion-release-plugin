---
hide:
- toc
---

Gradle release and version management plugin documentation.
See [GitHub project](http://github.com/allegro/axion-release-plugin)
for the source code and motivation for creating this plugin.

## Basic workflow

Basic workflow with `axion-release`:

```bash
$ git tag
project-0.1.0

$ ./gradlew currentVersion
0.1.0

$ git commit -m "Some commit."

$ ./gradlew currentVersion
0.1.1-SNAPSHOT

$ ./gradlew release

$ git tag
project-0.1.0 project-0.1.1

$ ./gradlew currentVersion
0.1.1

$ ./gradlew publish
published project-0.1.1 release version

$ ./gradlew markNextVersion -Prelease.version=1.0.0

$ ./gradlew currentVersion
1.0.0-SNAPSHOT
```

Note: `publish` task comes from Gradle [maven-publish](https://docs.gradle.org/current/userguide/publishing_maven.html)
plugin.
