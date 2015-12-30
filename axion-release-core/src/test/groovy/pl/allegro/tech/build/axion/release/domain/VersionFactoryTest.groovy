package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.domain.properties.TagPropertiesBuilder.tagProperties
import static pl.allegro.tech.build.axion.release.domain.properties.VersionPropertiesBuilder.versionProperties

class VersionFactoryTest extends Specification {

    VersionFactory factory = new VersionFactory()

    VersionProperties defaultVersionRules = versionProperties().build()

    TagProperties defaultTagRules = tagProperties().build()

    NextVersionProperties nextVersionRules = new NextVersionProperties(suffix: 'alpha', separator: '-', deserializer: NextVersionSerializer.DEFAULT.deserializer)

    def "should return current version read from position"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-1.0.0', true),
                nextVersionRules
        )

        when:
        Version version = factory.create(context, defaultVersionRules, defaultTagRules, nextVersionRules)

        then:
        version.toString() == '1.0.0'
    }

    def "should return forced version when forcing is on"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-1.0.0', true),
                nextVersionRules
        )
        VersionProperties versionRules = new VersionProperties(forcedVersion: '2.0.0')

        when:
        Version version = factory.create(context, versionRules, defaultTagRules, nextVersionRules)

        then:
        version.toString() == '2.0.0'
    }

    def "should return custom initial version when no tag exists"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', null, false),
                nextVersionRules
        )
        TagProperties tagRules = new TagProperties(initialVersion: { r, p -> '0.0.1' })

        when:
        Version version = factory.create(context, defaultVersionRules, tagRules, nextVersionRules)

        then:
        version.toString() == '0.0.1'
    }

    def "should return default initial version when no tag exists and initial version not explicitly defined"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', null, false),
                nextVersionRules
        )

        when:
        Version version = factory.create(context, defaultVersionRules, defaultTagRules, nextVersionRules)

        then:
        version.toString() == '0.1.0'
    }

    def "should deserialize nextVersion before deserializing version when on nextVersion tag"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-2.0.0-alpha', false),
                nextVersionRules
        )

        when:
        Version version = factory.create(context, defaultVersionRules, defaultTagRules, nextVersionRules)

        then:
        version.toString() == '2.0.0'
    }

    def "should not increment patch version when being on position after next version tag"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-2.0.0-alpha', false),
                nextVersionRules
        )

        when:
        Version version = factory.create(context, defaultVersionRules, defaultTagRules, nextVersionRules)

        then:
        version.toString() == '2.0.0'
    }

    def "should increment patch version when there are uncommitted changes and not ignoring them, even when on tag"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-1.0.0', true, true),
                nextVersionRules
        )
        VersionProperties versionRules = versionProperties().dontIgnoreUncommittedChanges().build()

        when:
        Version version = factory.create(context, versionRules, defaultTagRules, nextVersionRules)

        then:
        version.toString() == '1.0.1'
    }

    def "should not increment patch version when there are uncommitted changes but they are ignored (default)"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-1.0.0', true, true),
                nextVersionRules
        )

        when:
        Version version = factory.create(context, defaultVersionRules, defaultTagRules, nextVersionRules)

        then:
        version.toString() == '1.0.0'
    }

    def "should increment version when forced snapshot is on"() {
        given:
        ScmPositionContext context = new ScmPositionContext(
                new ScmPosition('master', 'release-1.0.0', true),
                nextVersionRules
        )
        VersionProperties versionRules = versionProperties().forceSnapshot().build()

        when:
        Version version = factory.create(context, versionRules, defaultTagRules, nextVersionRules)

        then:
        version.toString() == '1.0.1'
    }
}
