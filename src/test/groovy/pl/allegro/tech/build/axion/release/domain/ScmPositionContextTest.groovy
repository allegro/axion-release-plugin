package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Specification

class ScmPositionContextTest extends Specification {

    NextVersionConfig config = new NextVersionConfig(suffix: 'alpha')
    
    def "should create position with/without next version marker depending on position"() {
        expect:
        !new ScmPositionContext(ScmPosition.onTag('1.0.0'), config).nextVersionTag
        new ScmPositionContext(ScmPosition.onTag('1.0.0-alpha'), config).nextVersionTag
    }
}
