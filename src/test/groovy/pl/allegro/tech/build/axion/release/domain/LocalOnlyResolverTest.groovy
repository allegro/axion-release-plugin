package pl.allegro.tech.build.axion.release.domain

import spock.lang.Specification

class LocalOnlyResolverTest extends Specification {

    def "should resolve to localOnly when base value is true property present"() {
        expect:
        new LocalOnlyResolver(true).localOnly(false)
    }

    def "should resolve to localOnly when remote is not attached"() {
        expect:
        new LocalOnlyResolver(false).localOnly(false)
    }

    def "should not resolve to localOnly remote is present"() {
        expect:
        !new LocalOnlyResolver(false).localOnly(true)
    }
}
