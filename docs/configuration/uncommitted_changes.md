# Uncommitted changes

By default `axion-release` uses snapshot version only when there are
some commits after tag. Sometimes it might be useful to scan for
uncommitted changes and use snapshot version if they are present.

In order to treat uncommitted changes as trigger for version increment,
change setting:

    scmVersion {
        ignoreUncommittedChanges.set(false)
    }

This changes behavior from:

    # ./gradlew currentVersion
    1.0.0

    # cat "hello" > some_uncommitted_file

    # ./gradlew currentVersion
    1.0.0

to:

    # ./gradlew currentVersion
    1.0.0

    # cat "hello" > some_uncommitted_file

    # ./gradlew currentVersion
    1.0.1-SNAPSHOT

You can always override this option using command line:

    # ./gradlew currentVersion -Prelease.ignoreUncommittedChanges
    1.0.0
