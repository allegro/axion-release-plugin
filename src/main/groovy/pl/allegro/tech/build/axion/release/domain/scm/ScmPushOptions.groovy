package pl.allegro.tech.build.axion.release.domain.scm

import groovy.transform.Immutable


@Immutable
class ScmPushOptions {

    String remote
    
    boolean pushTagsOnly
    
}
