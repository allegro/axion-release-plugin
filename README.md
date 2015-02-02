axion-release-plugin
====

*gradle release and version management plugin*

[![Build Status](https://travis-ci.org/allegro/axion-release-plugin.svg?branch=master)](https://travis-ci.org/allegro/axion-release-plugin)

Releasing versions in Gradle is very different from releasing in Maven. Maven came with
[maven-release-plugin](http://maven.apache.org/maven-release/maven-release-plugin/) which
did all the dirty work. Gradle has no such tool and probably doesn't need it anyway. Evolution of software craft came
to the point, when we start thinking about SCM as ultimate source of truth about project version. Version should not be
hardcoded in **pom.xml** or **build.gradle**.

**axion-release-plugin** embraces this philosophy. Instead of reading project version from buildfile, it is derived
from nearest tag in SCM (or set to default if nothing was tagged). If current commit is tagged commit, project has
a release version. If there were any commits after last tag, project is in SNAPSHOT version. This very simple and
intuitive philosophy makes it a lot easier to manage project versions along SCM tag versions.

## Why write new plugin?

There are a few plugins that try to do the same - question is do we need another one?

* [build-version-plugin](https://github.com/GeoNet/gradle-build-version-plugin/) - plugin that was main source of inspiration, reads build version but lacks release options
* [ari gradle-release-plugin](https://github.com/ari/gradle-release-plugin) - takes only branch/tag name for version
* [townsfolk gradle-release-plugin](https://github.com/townsfolk/gradle-release) - more oldschhol, maven-release-plugin-like approach

What I needed was plugin that exposes version taken from nearest tag (like **build-version-plugin**) which at the same time
will be easily integrated with maven-publish and signing. It also needs to be Continuous Integration-aware.

## Usage

Apply plugin **gradle 2.1+** style:

```groovy
buildscript {
    repositories {
        mavenCentral() // for plugin dependencies
    }
}

plugins {
    id 'pl.allegro.tech.build.axion-release' version '1.1.0'
}
```

If using gradle < 2.1:

```groovy
buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath group: 'pl.allegro.tech.build', name: 'axion-release-plugin', version: '1.1.0'
    }
}

apply plugin: 'pl.allegro.tech.build.axion-release'
```

Basic configuration:

```groovy
// configure version
scmVersion {
    tag {
        prefix = 'tag-prefix'
    }
}

// make our version available to all
project.version = scmVersion.version
```

**Warning** Order of definitions in `build.gradle` file does matter! First you apply plugin, then comes `scmVersion { }`
closure if configuration is needed and only then you can use `scmVersion.version` to extract current version.

### Multi-project builds

For multi project builds the plugin has to be applied only on the root project, but version has to be set also in submodules.

```groovy
plugins {
    id 'pl.allegro.tech.build.axion-release' version '...'
}

allprojects {
    project.version = scmVersion.version
}
```

## Tasks

* `currentVersion` - prints current version as seen by plugin.
* `verifyRelease` - check some basic stuff before release, i.e. if there are no uncommitted files and if branch is not ahead of origin
* `release` - create tag with current version and push it to remote

## Example

```
# git tag
project-0.1.0

# ./gradlew currentVersion
0.1.0

# git commit -m "Some commit."

# ./gradlew currentVersion
0.1.1-SNAPSHOT

# ./gradlew release

# git tag
project-0.1.0 project-0.1.1

# ./gradlew currentVersion
0.1.1

# ./graldew publish
published project-0.1.1 release version

# ./gradlew markNextVersion -Prelease.nextVersion=1.0.0

# ./gradlew currentVersion
1.0.0-SNAPSHOT
```

## Options

### Authorization

Almost all authorization mechanisms are provided by [grgit](https://github.com/ajoberstar/grgit),
see [authorization docs](http://ajoberstar.org/grgit/docs/groovydoc/org/ajoberstar/grgit/auth/AuthConfig.html) for more info.

If you want to use custom key file to authorize, see *Using custom SSH key* section below.


### Command line

#### Mark next version

To start using new version without releasing, you can create next version marker tag:

```
./gradlew markNextVersion -Prelease.nextVersion=1.0.0
```

This will change version numbering, but plugin will treat current version as *SNAPSHOT*.
Marking next version means creating suffixed tag in repository. You can change the way it is created in `nextVersion` config section.

Usage scenario:

```
# ./gradlew currentVersion
0.1.0-SNAPSHOT

# ./gradlew markNextVersion -Prelease.nextVersion=1.0.0

# ./gradlew currentVersion
1.0.0-SNAPSHOT

# git tag
release-1.0.0-alpha
```

#### Force version

To force version, set `release.forceVersion` project property, for example to start with new 2.0.0 version:

```
./gradlew release -Prelease.forceVersion=2.0.0
```

Plugin recognizes if you are on tag or not and adds additional "-SNAPSHOT" suffix when necessary.
This property is ignored if it has empty value.

#### Local build

If you don't want release plugin to connect to remote, use `release.localOnly` flag:

```
./gradlew release -Prelease.localOnly
```

This implies, that plugin won't try to verify if current branch is ahead of remote nor it will push tag to remote.

#### Dry run

To check how release would behave without actually releasing anything, use `release.dryRun` option:

```
./gradlew release -Prelease.dryRun
```

This will output actions it would take during release, but won't persist changes.

#### Disabling checks

By default plugin checks if there are no uncommitted changes and if local branch is ahead of remote before release. These
checks can be disabled from command line using:

    * `release.disableChecks` - disable all checks
    * `release.disableUncommittedCheck` - disable uncommitted changes check
    * `release.disableRemoteCheck` - disable ahead of remote check

```
./gradlew release -Prelease.disableChecks // disable all checks
```

#### Attaching to remote

When running release in CI environment (as it should be always done!) it might be necessary to attach remote before pushing
the tag - this is the case of Atlassian Bamboo CI, which does very shallow fetch of repo, not clone. To do this, pass
`release.attachRemote` property, which value should be url to remote. Remote name will be taken from configuration.

```
./gradlew release -Prelease.attachRemote="ssh://git@hello.com/repo.git"
```

#### Using custom SSH key

Use `release.customKeyFile` and `release.customKeyPassword` properties to force Git to use custom SSH keys to authorize in
remote repository.

```
./gradlew release -Prelease.customKeyFile="./keys/secret_key_rsa" -Prelease.customKeyPassword=password
```

If you don't want to pass these information via command line, you cen set it in Gradle runtime by changing
`scmVersion.repository.customKey` and `scmVersion.repository.customKeyPassword` properties.

### Plugin

```groovy
scmVersion {

    repository {
        type = 'git' // type of repository, only git supported
        directory = project.rootProject.file('./') // where is repository root? by default rootProject dir
        remote = 'myRemote' // 'origin' by default

        customKey = 'AAasaDDSSD...' or project.file('myKey') // custom key - String or File
        customKeyPassword = 'secret' // custom key password
    }

    localOnly = false // never connect to remote (e.g. don't push tags), false by default

    sanitizeVersion = true // should created version be sanitized, true by default

    tag {
        prefix = 'tag-prefix' // prefix to be used, 'release' by default
        versionSeparator = '-' // separator between prefix and version number, '-' by default
        serialize = { tag, version -> rules.prefix + rules.versionSeparator + version } // creates tag name from raw version
        deserialize = { tag, position -> /* ... */ } // reads raw version from tag
        initialVersion = { tag, position, tagName -> /* ... */ } // returns initial version if none found, 0.1.0 by default
    }
    
    nextVersion {
        suffix = 'alpha' // tag suffix
        separator = '-' // separator between version and suffix
        serializer = { nextVersionConfig, version -> /* ... */ } // append suffix to version tag
        deserializer = { nextVersionConfig, position -> /* ... */ } // strip suffix off version tag
    }

    versionCreator { version, position -> /* ... */ } // creates version visible for Gradle from raw version and current position in scm
    versionCreator 'versionWithBranch' // use one of predefined version creators

    createReleaseCommit true // should create empty commit to annotate release in commit history, false by default
    releaseCommitMessage { version, position -> /* ... */ } // custom commit message if commits are created

    branchVersionCreators = [
        'feature/.*': { version, position -> /* ... */ },
        'bugfix/.*': { version, position -> /* ... */ }
    ] // map [regexp: closure] of version creators per branch, first match wins but no order is guaranteed (depends on created map instance)

    checks {
        uncommittedChanges = false // permanently disable uncommitted changes check
        aheadOfRemote = false // permanently disable ahead of remote check
    }
}
```

In `versionCreator` and `branchVersionCreators` closure arguments, `position` contains the following attributes:

* `latestTag` - the name of the latest tag
* `branch` - the name of the current branch
* `onTag` - true, if current commit is tagged with release version tag

#### Version creators

To create version creator that will attach branch name to version only for feature branches use:

```
branchVersionCreators = [
    'feature/.*': {version, position -> "$version-$position.branch"}
]
```

#### Predefined version creators

For convenience predefined version creators were created. They are registered under unique name (type) and aim to reduce
bloat in `build.gradle` for commonly used cases. Currently there are two predefined version creators:

* **default** returns version:

```
{version, position -> version}
```

* **versionWithBranch** appends branch name to version when not on master:

```
{version, position ->
    if(position.branch != 'master') {
        return version + '-' + position.branch
    }
    return version
}
```

#### Version sanitization

By default all versions are sanitized i.e. all characters that do not match `[A-Za-z0-9._-]` group are replaced with `-`. For example:

```
versionCreator = {version, position -> "$version-$position.branch"}
```

```
$ git branch
feature/some_feature

$ ./gradlew cV
release-0.1.0-feature-some_feature-SNAPSHOT
```

#### Create commit on release

By default **axion-release-plugin** operates on tags only and does not mess with commit history. However, in some cases it
might be useful to create additional commit to mark release. Use `createReleaseCommit` option to change this behavior.

Default commit message is created using closure:

```groovy
{ version, position ->
    "release version: $version"
}
```

It can be changed by overriding `releaseCommitMessage` property with own closure.

**Warning** don't use it as a way to commit files along with release - release commit does not run
`git add .` so nothing will be added to tracked changes set.

#### Tag name serializer

Tag name serializer interprets tag name and extracts version from it.
Tag name deserializer creates version based on rules and current version.

Default serializer extracts version from tag by removing prefix and version separator
from tag name. If prefix is empty, no version separator is used, e.g.:

```
tag: release-0.1.0, prefix: release, versionSeparator: - => version: 0.1.0
tag: 0.1.0, prefix: <empty>, versionSeparator: - => version: 0.1.0
```

Deserializer reverts this operation:

```
version: 0.1.0, prefix: release, versionSeparator: - => version: release-0.1.0
version: 0.1.0, prefix: <empty>, versionSeparator: - => tag: 0.1.0
```

## Publishing released version

Publishing release version is simple with **axion-release-plugin**. Since release does not increase version unless
you commit something, you can publish release version any time by calling gradle once again:

```
./gradlew release
./gradlew publish
```

Why not make it work in single Gradle run? **maven-publish** plugin reads **project.version** in configuration phase.
Any change made by tasks running prior to publishing won't be recognized.

## License

**axion-release-plugin** is published under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
