package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.RepositoryBasedTest
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties

import static pl.allegro.tech.build.axion.release.domain.properties.NextVersionPropertiesBuilder.nextVersionProperties
import static pl.allegro.tech.build.axion.release.domain.properties.TagPropertiesBuilder.tagProperties

class NextVersionMarkerTest extends RepositoryBasedTest {
    
    VersionService versionService

    NextVersionMarker nextVersionMarker

    def setup() {
        repository = context.repository()
        versionService = context.versionService()

        nextVersionMarker = new NextVersionMarker(context.scmService())
    }
    
    def "should create next version tag with given version"() {
        given:
        TagProperties tagRules = tagProperties().build()
        NextVersionProperties rules = nextVersionProperties().withNextVersion('2.0.0').build()

        when:
        nextVersionMarker.markNextVersion(rules, tagRules)
        
        then:
        repository.latestTags(~/.*/).tags == ['release-2.0.0-alpha']
    }
}
