package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.domain.scm.ScmPositionBuilder.scmPosition

class TagMessageCreatorTest extends Specification {

    static final TagProperties.MessageCreator DEFAULT_MESSAGE_CREATOR = TagMessageCreator.DEFAULT.messageCreator
    static final ScmPosition MAIN = scmPosition('main')

    def "default message creator should return empty messsage"() {
        given:
        String version = '1.0.0'
        String previousVersion = '0.1.0'

        when:
        String message = DEFAULT_MESSAGE_CREATOR.apply(MAIN, version, previousVersion)

        then:
        message == ''
    }
}
