package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

enum PredefinedVersionCreator {

    DEFAULT('default', { String versionFromTag, ScmPosition position ->
        return versionFromTag.toString()
    }),

    VERSION_WITH_BRANCH('versionWithBranch', { String versionFromTag, ScmPosition position ->
        if (position.branch != 'master') {
            return "$versionFromTag-$position.branch"
        }
        return versionFromTag
    })

    private final String type

    final Closure versionCreator

    private PredefinedVersionCreator(String type, Closure c) {
        this.type = type
        this.versionCreator = c
    }

    static Closure versionCreatorFor(String type) {
        PredefinedVersionCreator creator = values().find { it.type == type }
        if (creator == null) {
            throw new IllegalArgumentException("There is no predefined version creator with $type type. " +
                    "You can choose from: ${values().collect { it.type }}");
        }
        return creator.versionCreator
    }
}
