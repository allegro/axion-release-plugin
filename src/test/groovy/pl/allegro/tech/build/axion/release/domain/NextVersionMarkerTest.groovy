package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.RepositoryBasedTest
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHooksRunner
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

import static pl.allegro.tech.build.axion.release.domain.properties.NextVersionPropertiesBuilder.nextVersionProperties
import static pl.allegro.tech.build.axion.release.domain.properties.TagPropertiesBuilder.tagProperties

class NextVersionMarkerTest extends RepositoryBasedTest {

    VersionService versionService

    NextVersionMarker nextVersionMarker

    private Releaser releaser

    Project project = ProjectBuilder.builder().build()

    VersionConfig config = new VersionConfig(project)

    def setup() {
        repository = context.repository()
        versionService = context.versionService()
        ScmService scmService = context.scmService()
        VersionService versionService = context.versionService()

        releaser = new Releaser(versionService, scmService, new ReleaseHooksRunner(versionService, scmService))

        nextVersionMarker = new NextVersionMarker(context.scmService())
    }

    def "should create next version tag with given version"() {
        given:
        TagProperties tagRules = tagProperties().build()
        NextVersionProperties rules = nextVersionProperties().withNextVersion('2.0.0').build()

        when:
        nextVersionMarker.markNextVersion(rules, tagRules, config)

        then:
        repository.latestTags(~/.*/).tags == ['release-2.0.0-alpha']
    }

    def "should create next version with default incrementer"() {
        given:
        TagProperties tagRules = tagProperties().build()
        NextVersionProperties rules = nextVersionProperties().build()

        when:
        nextVersionMarker.markNextVersion(rules, tagRules, config)

        then:
        repository.latestTags(~/.*/).tags == ['release-0.1.1-alpha']
    }

    def "should create next version with major incrementer"() {
        given:
        TagProperties tagRules = tagProperties().build()
        NextVersionProperties rules = nextVersionProperties()
            .withVersionIncrementer("incrementMajor")
            .build()

        when:
        nextVersionMarker.markNextVersion(rules, tagRules, config)

        then:
        repository.latestTags(~/.*/).tags == ['release-1.0.0-alpha']
    }
}
