package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import static pl.allegro.tech.build.axion.release.domain.PredefinedVersionIncrementer.*

import com.github.zafarkhaja.semver.Version

import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

class PredefinedVersionIncrementerTest extends Specification {

    VersionIncrementerContext context = new VersionIncrementerContext(Version.valueOf('0.1.0'), ScmPosition.defaultPosition())

    def "should increment patch when incrementPatch rule used"() {
        expect:
        versionIncrementerFor('incrementPatch')(context) == Version.valueOf('0.1.1')
    }

    def "should increment minor when incrementMinor rule used"() {
        expect:
        versionIncrementerFor('incrementMinor')(context) == Version.valueOf('0.2.0')
    }

    def "should increment major when incrementMajor rule used"() {
        expect:
        versionIncrementerFor('incrementMajor')(context) == Version.valueOf('1.0.0')
    }

    def "should increment minor if not on release branch and incrementMinorIfNotOnRelease used"() {
        expect:
        versionIncrementerFor('incrementMinorIfNotOnRelease', [releaseBranchPattern: 'release.*'])(context) == Version.valueOf('0.2.0')
    }

    def "should increment patch if on release branch and incrementMinorIfNotOnRelease used"() {
        expect:
        versionIncrementerFor('incrementMinorIfNotOnRelease', [releaseBranchPattern: 'master'])(context) == Version.valueOf('0.1.1')
    }

    def "should delegate to first matching incrementer when branchSpecific rule used"() {
        expect:
        versionIncrementerFor('branchSpecific', ['release.*': 'incrementPatch', 'master': 'incrementMinor'])(context) == Version.valueOf('0.2.0')
    }

    def "should increment prerelease version when incrementPrerelease rule used"() {
        given:
        VersionIncrementerContext context = new VersionIncrementerContext(Version.valueOf('0.1.0-rc1'), ScmPosition.defaultPosition())

        expect:
        versionIncrementerFor('incrementPrerelease')(context) == Version.valueOf('0.1.0-rc2')
    }

    def "should increment prerelease version even when it has leading zeroes when incrementPrerelease rule used"() {
        given:
        VersionIncrementerContext context = new VersionIncrementerContext(Version.valueOf('0.1.0-rc01'), ScmPosition.defaultPosition())

        expect:
        versionIncrementerFor('incrementPrerelease')(context) == Version.valueOf('0.1.0-rc02')
    }

    def "should create prerelease for next major version when createMajorRC rule used"() {
        given:
        VersionIncrementerContext context = new VersionIncrementerContext(Version.valueOf('0.1.0'), ScmPosition.defaultPosition())

        expect:
        versionIncrementerFor('createMajorRC')(context) == Version.valueOf('1.0.0-RC1')
    }

    def "should throw exception if already an RC version when createMajorRC rule used"() {
        given:
        VersionIncrementerContext context = new VersionIncrementerContext(Version.valueOf('0.1.0-rc1'), ScmPosition.defaultPosition())

        when:
        versionIncrementerFor('createMajorRC')(context)

        then:
        IllegalArgumentException e = thrown(IllegalArgumentException)
        e.message == "Already on a prerelease, use incrementPrerelease"
    }

    def "should create prerelease for next minor version when createMinorRC rule used"() {
        given:
        VersionIncrementerContext context = new VersionIncrementerContext(Version.valueOf('0.1.0'), ScmPosition.defaultPosition())

        expect:
        versionIncrementerFor('createMinorRC')(context) == Version.valueOf('0.2.0-RC1')
    }

    def "should throw exception if already an RC version when createMinorRC rule used"() {
        given:
        VersionIncrementerContext context = new VersionIncrementerContext(Version.valueOf('0.1.0-rc1'), ScmPosition.defaultPosition())

        when:
        versionIncrementerFor('createMinorRC')(context)

        then:
        IllegalArgumentException e = thrown(IllegalArgumentException)
        e.message == "Already on a prerelease, use incrementPrerelease"
    }

    @Unroll
    def "should create #finalVersion for #rcVersion when createFinal rule used"(String rcVersion, String finalVersion) {
        given:
        VersionIncrementerContext context = new VersionIncrementerContext(Version.valueOf(rcVersion), ScmPosition.defaultPosition())

        expect:
        versionIncrementerFor('createFinal')(context) == Version.valueOf(finalVersion)

        where:
        rcVersion << ['0.1.0-rc1', '1.0.0-rc1', '1.1.0-rc2']
        finalVersion << ['0.1.0', '1.0.0', '1.1.0']
    }

    def "should throw error if not on rc when createFinal rule used"() {
        given:
        VersionIncrementerContext context = new VersionIncrementerContext(Version.valueOf('0.1.0'), ScmPosition.defaultPosition())

        when:
        versionIncrementerFor('createFinal')(context)

        then:
        IllegalArgumentException e = thrown(IllegalArgumentException)
        e.message == "Not on a prerelease, use normal release instead"
    }

    def "should throw exception if already an RC version when createFinal rule used"() {
        given:
        VersionIncrementerContext context = new VersionIncrementerContext(Version.valueOf('0.1.0-rc1'), ScmPosition.defaultPosition())

        when:
        versionIncrementerFor('createMinorRC')(context)

        then:
        IllegalArgumentException e = thrown(IllegalArgumentException)
        e.message == "Already on a prerelease, use incrementPrerelease"
    }

    def "should increment prerelease when incrementPrereleaseOrMinor rule used"() {
        given:
        VersionIncrementerContext context = new VersionIncrementerContext(Version.valueOf('0.1.0-rc2'), ScmPosition.defaultPosition())

        expect:
        versionIncrementerFor('incrementPrereleaseOrMinor')(context) == Version.valueOf('0.1.0-rc3')
    }

    def "should increment minor when not on prerelease when incrementPrereleaseOrMinor rule used"() {
        given:
        VersionIncrementerContext context = new VersionIncrementerContext(Version.valueOf('0.1.0'), ScmPosition.defaultPosition())

        expect:
        versionIncrementerFor('incrementPrereleaseOrMinor')(context) == Version.valueOf('0.2.0')
    }

    def "should throw exception when unknown incrementer used"() {
        when:
        versionIncrementerFor('unknown')(context)

        then:
        thrown(IllegalArgumentException)
    }
}
