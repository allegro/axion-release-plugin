package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

enum PredefinedVersionCreator {

    SIMPLE('simple', { String versionFromTag, ScmPosition position, VersionContext versionContext ->
        return versionFromTag
    }),

    VERSION_WITH_BRANCH('versionWithBranch', { String versionFromTag, ScmPosition position, VersionContext versionContext ->
        if ((position.branch != 'master' && position.branch != 'main') && position.branch != 'HEAD') {
            return "$versionFromTag-$position.branch".toString()
        }
        return versionFromTag
    }),

    VERSION_WITH_BRANCH_WHEN_SNAPSHOT('versionWithBranchWhenSnapshot', { String versionFromTag, ScmPosition position, VersionContext versionContext ->
        if ((versionContext.position.branch != 'master' && versionContext.position.branch != 'main') && versionContext.position.branch != 'HEAD' &&
            versionContext.isSnapshot()) {
            return "$versionFromTag-$versionContext.position.branch".toString()
        }
        return versionFromTag
    }),

    VERSION_WITH_COMMIT_HASH('versionWithCommitHash', { String versionFromTag, ScmPosition position, VersionContext versionContext ->
    if ((position.branch != 'master' && position.branch != 'main') && position.branch != 'HEAD') {
        return "$versionFromTag-$position.shortRevision".toString()
    }
    return versionFromTag
    })

    private final String type

    final VersionProperties.VersionCreator versionCreator

    private PredefinedVersionCreator(String type, Closure c) {
        this.type = type
        this.versionCreator = c
    }

    static VersionProperties.VersionCreator versionCreatorFor(String type) {
        PredefinedVersionCreator creator = values().find { it.type == type }
        if (creator == null) {
            throw new IllegalArgumentException("There is no predefined version creator with $type type. " +
                    "You can choose from: ${values().collect { it.type }}");
        }
        return creator.versionCreator
    }

}
