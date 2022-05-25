package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

import java.util.function.BiFunction

enum PredefinedReleaseCommitMessageCreator {

    DEFAULT('default', { String version, ScmPosition position ->
        return "release version: $version".toString()
    });

    private final String type

    final CommitMessageCreator commitMessageCreator

    private PredefinedReleaseCommitMessageCreator(String type, Closure c) {
        this.type = type
        this.commitMessageCreator = c
    }

    static CommitMessageCreator commitMessageCreatorFor(String type) {
        PredefinedReleaseCommitMessageCreator creator = PredefinedReleaseCommitMessageCreator.values().find { it.type == type }
        if (creator == null) {
            throw new IllegalArgumentException("There is no predefined commit message creator with $type type. " +
                    "You can choose from: ${PredefinedReleaseCommitMessageCreator.values().collect { it.type }}");
        }
        return creator.commitMessageCreator
    }

    interface CommitMessageCreator extends BiFunction<String,ScmPosition,String> {}
}
