package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.RepositoryBasedTest

class NextVersionMarkerTest extends RepositoryBasedTest {
    
    VersionService versionService

    NextVersionMarker nextVersionMarker
    
    def setup() {
        repository = context.repository()
        versionService = context.versionService()

        nextVersionMarker = new NextVersionMarker(context.scmService(), context.localOnlyResolver(), project.logger)
    }
    
    def "should create next version tag with given version"() {
        when:
        nextVersionMarker.markNextVersion(config, '2.0.0')
        
        then:
        repository.currentPosition(~/.*/).latestTag == 'release-2.0.0-alpha'
        versionService.currentVersion(config, VersionReadOptions.defaultOptions()).version.toString() == '2.0.0-SNAPSHOT'
    }
}
