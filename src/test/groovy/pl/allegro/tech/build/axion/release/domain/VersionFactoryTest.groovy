package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties
import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.domain.ScmStateBuilder.scmState
import static pl.allegro.tech.build.axion.release.domain.properties.TagPropertiesBuilder.tagProperties
import static pl.allegro.tech.build.axion.release.domain.properties.VersionPropertiesBuilder.versionProperties
import static pl.allegro.tech.build.axion.release.domain.scm.ScmPositionBuilder.scmPosition

class VersionFactoryTest extends Specification {

    VersionProperties versionProperties = versionProperties().build()

    TagProperties tagProperties = tagProperties().build()

    NextVersionProperties nextVersionProperties = new NextVersionProperties(suffix: 'alpha', separator: '-', deserializer: NextVersionSerializer.DEFAULT.deserializer)

    VersionFactory factory = versionFactory(versionProperties)

    def "should return current version read from tag"() {
        when:
        Version version = factory.versionFromTag('release-1.0.0')

        then:
        version.toString() == '1.0.0'
    }

    def "should deserialize nextVersion before deserializing version when on nextVersion tag"() {
        when:
        Version version = factory.versionFromTag('release-2.0.0-alpha')

        then:
        version.toString() == '2.0.0'
    }

    def "should capture parse exception and output meaningful message"() {
        when:
        Version version = factory.versionFromTag('release-blabla-1.0.0')

        then:
        thrown(VersionFactory.TagParseException)
    }

    def "should create initial version"() {
        when:
        Version version = factory.initialVersion()

        then:
        version.toString() == '0.1.0'
    }

    def "should increment patch version when not on tag when creating final version on default settings"() {
        when:
        Map version = factory.createFinalVersion(scmState().build(), Version.valueOf('1.0.0'))

        then:
        version.version.toString() == '1.0.1'
        version.snapshot
    }

    def "should return forced version when forcing is on"() {
        given:
        VersionFactory factory = versionFactory(versionProperties().forceVersion('1.5.0').build())

        when:
        Map version = factory.createFinalVersion(scmState().build(), Version.valueOf('1.0.0'))

        then:
        version.version.toString() == '1.5.0'
        version.snapshot
    }

    def "should return forced snapshot version when forcing is on and already on tag"() {
        given:
        VersionFactory factory = versionFactory(versionProperties().forceVersion('1.5.0').build())

        when:
        Map version = factory.createFinalVersion(scmState().onReleaseTag().build(), Version.valueOf('1.0.0'))

        then:
        version.version.toString() == '1.5.0'
        version.snapshot
    }

    def "should return forced version without snapshot when forcing same version as current tag"() {
        given:
        VersionFactory factory = versionFactory(versionProperties().forceVersion('1.5.0').build())

        when:
        Map version = factory.createFinalVersion(scmState().onReleaseTag().build(), Version.valueOf('1.5.0'))

        then:
        version.version.toString() == '1.5.0'
        !version.snapshot
    }

    def "should not increment patch version when being on position after next version tag"() {
        when:
        Map version = factory.createFinalVersion(scmState().onNextVersionTag().build(), Version.valueOf('1.0.0'))

        then:
        version.version.toString() == '1.0.0'
        version.snapshot
    }

    def "should not increment patch version when on tag and there are uncommitted changes but they are ignored (default)"() {
        when:
        Map version = factory.createFinalVersion(scmState().onReleaseTag().hasUncomittedChanges().build(), Version.valueOf('1.0.0'))

        then:
        version.version.toString() == '1.0.0'
        !version.snapshot
    }

    def "should increment patch version when there are uncommitted changes and not ignoring them, even when on tag"(VersionProperties versionProps) {
        given:
        VersionFactory factory = versionFactory(versionProps)

        when:
        Map version = factory.createFinalVersion(scmState().hasUncomittedChanges().build(), Version.valueOf('1.0.0'))

        then:
        version.version.toString() == '1.0.1'
        version.snapshot

        where:
        versionProps << [
            versionProperties().dontIgnoreUncommittedChanges().build(),
            versionProperties().dontIgnoreUncommittedChanges().forceSnapshot().build()
        ]
    }

    def "should increment version when forced snapshot is on" (ScmState scmStateParam) {
        given:
        VersionFactory factory = versionFactory(versionProperties().forceSnapshot().build())

        when:
        Map version = factory.createFinalVersion(scmStateParam, Version.valueOf('1.0.0'))

        then:
        version.version.toString() == '1.0.1'
        version.snapshot
        
        where:
        scmStateParam << [
            scmState().build(),
            scmState().onReleaseTag().build(), // on release tag
            scmState().onReleaseTag().hasUncomittedChanges().build(), // with uncomitted changes
        ]
    }

    def "should not increment version when forced snapshot is on and is on nextVersion tag"() {
        given:
        VersionFactory factory = versionFactory(versionProperties().forceSnapshot().build())

        when:
        // This simulates nextVersionTag == 'release-1.0.0-alpha'
        Map version = factory.createFinalVersion(scmState().onNextVersionTag().build(), Version.valueOf('1.0.0'))

        then:
        // So snapshot version of this is '1.0.0-SNAPSHOT'
        version.version.toString() == '1.0.0'
        version.snapshot
    }

    def "should not increment version when this is initial version"(VersionProperties versionProps) {
        given:
        VersionFactory factory = versionFactory(versionProps)

        when:
        Map version = factory.createFinalVersion(scmState().noReleaseTagsFound().build(), Version.valueOf('0.1.0'))

        then:
        version.version.toString() == '0.1.0'
        version.snapshot

        where:
        versionProps << [
            versionProperties().build(),
            versionProperties().forceSnapshot().build()
        ]

    }


    private VersionFactory versionFactory(VersionProperties versionProperties) {
        return new VersionFactory(versionProperties, tagProperties, nextVersionProperties, scmPosition('master'))
    }
}
