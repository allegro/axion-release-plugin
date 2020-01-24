# Local development

To publish snapshot version to Maven Local change `build.gradle`:

```
plugins {
    id 'maven-publish'
}
```

And run:

```
./gradlew publishToMavenLocal
```

To use version from Maven Local change `build.gradle`:

```
buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath "pl.allegro.tech.build:axion-release-plugin:1.10.4-SNAPSHOT"
    }
}

plugins {
// remove axion-release from here
}

apply plugin: "pl.allegro.tech.build.axion-release"
```

