package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

import java.util.function.BiFunction

enum PredefinedVersionCreator {

    SIMPLE('simple', { String versionFromTag, ScmPosition position ->
        return versionFromTag.toString()
    }),

    VERSION_WITH_BRANCH('versionWithBranch', { String versionFromTag, ScmPosition position ->
        if ((position.branch != 'master' && position.branch != 'main') && position.branch != 'HEAD') {
            return "$versionFromTag-$position.branch".toString()
        }
        return versionFromTag
    })

    private final String type

    final VersionProperties.Creator versionCreator

    private PredefinedVersionCreator(String type, Closure c) {
        this.type = type
        this.versionCreator = c
    }

    static VersionProperties.Creator versionCreatorFor(String type) {
        PredefinedVersionCreator creator = values().find { it.type == type }
        if (creator == null) {
            throw new IllegalArgumentException("There is no predefined version creator with $type type. " +
                    "You can choose from: ${values().collect { it.type }}");
        }
        return creator.versionCreator
    }

}
