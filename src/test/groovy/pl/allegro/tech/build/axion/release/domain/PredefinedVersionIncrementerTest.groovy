package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmPositionBuilder
import spock.lang.Specification

import static pl.allegro.tech.build.axion.release.domain.PredefinedVersionIncrementer.versionIncrementerFor
import static pl.allegro.tech.build.axion.release.domain.scm.ScmPositionBuilder.scmPosition

class PredefinedVersionIncrementerTest extends Specification {
    
    VersionIncrementerContext context = new VersionIncrementerContext(Version.valueOf('0.1.0'), scmPosition('master'))
    
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
        VersionIncrementerContext context = new VersionIncrementerContext(Version.valueOf('0.1.0-rc1'), scmPosition('master'))

        expect:
        versionIncrementerFor('incrementPrerelease')(context) == Version.valueOf('0.1.0-rc2')
    }

    def "should create prerelease version when incrementPrerelease rule used"() {
        given:
        VersionIncrementerContext context = new VersionIncrementerContext(Version.valueOf('0.1.0'), scmPosition('master'))

        expect:
        versionIncrementerFor('incrementPrerelease')(context) == Version.valueOf('0.1.1-rc1')
    }

    def "should increment prerelease version even when it has leading zeroes when incrementPrerelease rule used"() {
        given:
        VersionIncrementerContext context = new VersionIncrementerContext(Version.valueOf('0.1.0-rc01'), scmPosition('master'))

        expect:
        versionIncrementerFor('incrementPrerelease')(context) == Version.valueOf('0.1.0-rc02')
    }

    def "should throw exception when unknown incrementer used"() {
        when:
        versionIncrementerFor('unknown')(context)
        
        then:
        thrown(IllegalArgumentException)
    }
}
