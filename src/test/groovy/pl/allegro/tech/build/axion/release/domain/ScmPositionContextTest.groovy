package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Specification

class ScmPositionContextTest extends Specification {

    NextVersionProperties rules = new NextVersionProperties(suffix: 'alpha')

    def "should create position with/without next version marker depending on position"() {
        expect:
        !new ScmPositionContext(ScmPosition.onTag('1.0.0'), rules).nextVersionTag
        new ScmPositionContext(ScmPosition.onTag('1.0.0-alpha'), rules).nextVersionTag
    }
}
