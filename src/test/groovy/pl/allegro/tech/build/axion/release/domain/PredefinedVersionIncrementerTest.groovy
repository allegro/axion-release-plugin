package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.TagPrefixConf.*
import static pl.allegro.tech.build.axion.release.domain.PredefinedVersionIncrementer.versionIncrementerFor
import static pl.allegro.tech.build.axion.release.domain.scm.ScmPositionBuilder.scmPosition

class PredefinedVersionIncrementerTest extends Specification {

    VersionIncrementerContext context = new VersionIncrementerContext(Version.valueOf('0.1.0'), scmPosition('master'))

    def "should increment patch when incrementPatch rule used"() {
        expect:
        versionIncrementerFor('incrementPatch').apply(context) == Version.valueOf('0.1.1')
    }

    def "should increment minor when incrementMinor rule used"() {
        expect:
        versionIncrementerFor('incrementMinor').apply(context) == Version.valueOf('0.2.0')
    }

    def "should increment major when incrementMajor rule used"() {
        expect:
        versionIncrementerFor('incrementMajor').apply(context) == Version.valueOf('1.0.0')
    }

    def "should increment minor if not on release branch and incrementMinorIfNotOnRelease used"() {
        expect:
        versionIncrementerFor('incrementMinorIfNotOnRelease', [releaseBranchPattern: (defaultPrefix() +'.*')]).apply(context) == Version.valueOf('0.2.0')
    }

    def "should increment patch if on release branch and incrementMinorIfNotOnRelease used"() {
        expect:
        versionIncrementerFor('incrementMinorIfNotOnRelease', [releaseBranchPattern: 'master']).apply(context) == Version.valueOf('0.1.1')
    }

    def "should delegate to first matching incrementer when branchSpecific rule used"() {
        expect:
        versionIncrementerFor('branchSpecific', [(defaultPrefix() + '.*'): 'incrementPatch', 'master': 'incrementMinor']).apply(context) == Version.valueOf('0.2.0')
    }

    def "should increment prerelease version when incrementPrerelease rule used"() {
        given:
        VersionIncrementerContext context = new VersionIncrementerContext(Version.valueOf('0.1.0-rc1'), scmPosition('master'))

        expect:
        versionIncrementerFor('incrementPrerelease').apply(context) == Version.valueOf('0.1.0-rc2')
    }

    def "should increment patch version when incrementPrerelease rule used and currentVersion is not rc"() {
        given:
        VersionIncrementerContext context = new VersionIncrementerContext(Version.valueOf('0.1.0'), scmPosition('master'))

        expect:
        versionIncrementerFor('incrementPrerelease').apply(context) == Version.valueOf('0.1.1')
    }

    def "should create prerelease version when incrementPrerelease rule used with initialPreReleaseIfNotOnPrerelease"() {
        given:
        VersionIncrementerContext context = new VersionIncrementerContext(Version.valueOf('0.1.0'), scmPosition('master'))

        expect:
        versionIncrementerFor('incrementPrerelease', [initialPreReleaseIfNotOnPrerelease: 'rc1']).apply(context) == Version.valueOf('0.1.1-rc1')
    }

    def "should increment prerelease version even when it has leading zeroes when incrementPrerelease rule used"() {
        given:
        VersionIncrementerContext context = new VersionIncrementerContext(Version.valueOf('0.1.0-rc01'), scmPosition('master'))

        expect:
        versionIncrementerFor('incrementPrerelease').apply(context) == Version.valueOf('0.1.0-rc02')
    }

    def "should throw exception when unknown incrementer used"() {
        when:
        versionIncrementerFor('unknown')(context)

        then:
        thrown(IllegalArgumentException)
    }
}
