# Next version markers

By default `axion-release-plugin` reads last released version and
increments least significant (patch) version number to reflect changes.
In some cases though, you might want to mark start of work on next major
version (like 2.0.0) and release `2.0.0-SNAPSHOT` instead of
`1.5.1-SNAPSHOT`. Next version markers make it possible.

Next version marker is simply a specific tag. It ends with customizable
suffix and is handled differently. When reading version number and last
tag is next version marker, version is treated as SNAPSHOT, but version
is not incremented:

    # git tag
    v1.5.0

    # ./gradlew cV
    1.5.1-SNAPSHOT

    # ./gradlew markNextVersion -Prelease.version=2.0.0

    # git tag
    v1.5.0 v2.0.0-alpha

    # ./gradlew cV
    2.0.0-SNAPSHOT

    # ./gradlew markNextVersion

    # ./gradlew cV
    2.0.1-SNAPSHOT

    # ./gradlew markNextVersion -Prelease.incrementer=incrementMinor

    # ./gradlew cV
    2.1.0-SNAPSHOT

To create next version marker use `markNextVersion`. Without parameters next version will be created with default
version incrementer - incrementMinor, which can be overwritten with `release.incrementer` parameter.
Custom next version can be created along with command line option `release.version`.

Default next version marker serializer/deserializer can be customized
using `scmVersion.nextVersion.suffix` and
`scmVersion.nextVersion.separator` properties:

    scmVersion {
        nextVersion {
            suffix.set("alpha")
            separator.set("-")
        }
    }

As with normal tags, you can specify custom serializers and
deserializers for next version markers. See [Version Creation](version.md)
documentation to see what serializers and deserializers are:

    scmVersion {
        nextVersion {
            serializer( { nextVersionConfig, version -> ...})
            deserializer( { nextVersionConfig, position -> ...})
        }
    }
