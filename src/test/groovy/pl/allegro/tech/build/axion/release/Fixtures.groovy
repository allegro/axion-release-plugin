package pl.allegro.tech.build.axion.release

import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository

// Centralize setup for tests. This class is reachable from 'integration' and 'test' source tree.
final class Fixtures {
    // Split up Fixtures, so its easier to see what they are used for.
    // Shared fixtures can be in the Fixtures class itself (there are none at the hour of writing)
    final class FixtureUseGlobalVersion {
        static void setupABranchWithHighTagAndBBranchWithLowTag(ScmRepository repository) {
            // * (tag: v2.0.0, high) some commit 2
            // | * (HEAD -> low, tag: v1.0.1) some commit 3
            // |/
            // * (tag: v1.0.0, start) some commit  1
            // * (master) initial commit
            repository.branch('start')
            repository.checkout('start')
            repository.commit(['*'], 'some commit  1')
            repository.tag("${TagPrefixConf.fullPrefix()}1.0.0")
            repository.branch('high')
            repository.checkout('high')
            repository.commit(['*'], 'some commit 2')
            repository.tag("${TagPrefixConf.fullPrefix()}2.0.0")
            repository.checkout('start')
            repository.branch('low')
            repository.checkout('low')
            repository.commit(['*'], 'some commit 3')
            repository.tag("${TagPrefixConf.fullPrefix()}1.0.1")
        }
        static void setupABranchWithHighTagsOutOfOrderAndBBranchWithLowTag(ScmRepository repository) {
            // * 570120a (HEAD -> high, tag: v2.0.0) some commit 3
            // * 610acf2 (tag: v3.0.0) some commit 2
            // | * b510d23 (tag: v1.0.1, low) some commit 3
            // |/
            // * 0b4c7ac (tag: v1.0.0, start) some commit  1
            // * eeae3d9 (master) initial commit
            repository.branch('start')
            repository.checkout('start')
            repository.commit(['*'], 'some commit  1')
            repository.tag("${TagPrefixConf.fullPrefix()}1.0.0")
            repository.branch('high')
            repository.checkout('high')
            repository.commit(['*'], 'some commit 2')
            repository.tag("${TagPrefixConf.fullPrefix()}3.0.0")
            repository.commit(['*'], 'some commit 3')
            repository.tag("${TagPrefixConf.fullPrefix()}2.0.0")
            repository.checkout('start')
            repository.branch('low')
            repository.checkout('low')
            repository.commit(['*'], 'some commit 3')
            repository.tag("${TagPrefixConf.fullPrefix()}1.0.1")
        }
    }
}
