package pl.allegro.tech.build.axion.release

class PredefinedVersionIncrementerIntegrationTest extends BaseIntegrationTest {

    def "should increment patch when incrementPatch rule used"() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionIncrementer('incrementPatch')
                }
            }
        """
        createTag('v0.1.0')

        when:
        def result = runGradle('markNextVersion')

        then:
        result.output.contains('Creating next version marker tag: v0.1.1-alpha\n')
    }

    def "should increment minor when incrementMinor rule used"() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionIncrementer('incrementMinor')
                }
            }
        """

        createTag('v0.1.0')

        when:
        def result = runGradle('markNextVersion')

        then:
        result.output.contains('Creating next version marker tag: v0.2.0-alpha\n')
    }

    def "should increment major when incrementMajor rule used"() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionIncrementer('incrementMajor')
                }
            }
        """
        createTag('v0.1.0')

        when:
        def result = runGradle('markNextVersion')

        then:
        result.output.contains('Creating next version marker tag: v1.0.0-alpha\n')
    }

    def "should increment minor if not on release branch and incrementMinorIfNotOnRelease used"() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionIncrementer('incrementMinorIfNotOnRelease')
                }
            }
        """
        createTag('release-0.1.0')
        checkout('feature-branch')

        when:
        def result = runGradle('markNextVersion')

        then:
        result.output.contains('Creating next version marker tag: v0.2.0-alpha\n')
    }

    def "should increment patch if on release branch and incrementMinorIfNotOnRelease used"() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionIncrementer('incrementMinorIfNotOnRelease')
                }
            }
        """
        createTag('release-0.1.0')
        checkout("release")

        when:
        def result = runGradle('markNextVersion')

        then:
        result.output.contains('Creating next version marker tag: v0.2.0-alpha\n')
    }

    def "should delegate to first matching incrementer when branchSpecific rule used"() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionIncrementer('branchSpecific', [
                        'release-.*': 'incrementPatch',
                        'master': 'incrementMinor'
                    ])
                }
            }
        """
        createTag('release-0.1.0')
        checkout('release-feature')

        when:
        def result = runGradle('markNextVersion')

        then:
        result.output.contains('Creating next version marker tag: v0.1.1-alpha\n')
    }

    def "should increment prerelease version when incrementPrerelease rule used"() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionIncrementer('incrementPrerelease')
                }
            }
        """
        createTag('v0.1.0-rc1')

        when:
        def result = runGradle('markNextVersion')

        then:
        result.output.contains('Creating next version marker tag: v0.1.0-rc2-alpha\n')
    }

    def "should increment patch version when incrementPrerelease rule used and currentVersion is not rc"() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionIncrementer('incrementPrerelease')
                }
            }
        """
        createTag('v0.1.0')

        when:
        def result = runGradle('markNextVersion')

        then:
        result.output.contains('Creating next version marker tag: v0.1.1-alpha\n')
    }

    def "should create prerelease version when incrementPrerelease rule used with initialPreReleaseIfNotOnPrerelease"() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionIncrementer('incrementPrerelease', [initialPreReleaseIfNotOnPrerelease: 'rc1'])
                }
            }
        """
        createTag('v0.1.0')

        when:
        def result = runGradle('markNextVersion')

        then:
        result.output.contains('Creating next version marker tag: v0.1.1-rc1-alpha\n')
    }

    def "should increment prerelease version even when it has leading zeroes when incrementPrerelease rule used"() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionIncrementer('incrementPrerelease')
                }
            }
        """
        createTag('v0.1.0-rc01')

        when:
        def result = runGradle('markNextVersion')

        then:
        result.output.contains('Creating next version marker tag: v0.1.0-rc02-alpha\n')
    }

    def "should throw exception when unknown incrementer used"() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionIncrementer('unknown')
                }
            }
        """
        createTag('v0.1.0')

        when:
        def result = runGradleAndFail('markNextVersion')

        then:
        result.output.contains('There is no predefined version incrementer with unknown name.')
    }
}
