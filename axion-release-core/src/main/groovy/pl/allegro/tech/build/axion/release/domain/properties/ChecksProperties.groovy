package pl.allegro.tech.build.axion.release.domain.properties

import groovy.transform.Immutable

@Immutable
class ChecksProperties {

    final boolean checkUncommittedChanges

    final boolean checkAheadOfRemote

}
