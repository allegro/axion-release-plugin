package pl.allegro.tech.build.axion.release

class PredefinedVersionCreatorIntegrationTest extends BaseIntegrationTest {
    def "default version creator should just return version string"() {
        given:
        buildFile("""
            scmVersion {
                release {
                    versionCreator('simple')
                }
            }
        """)

        when:
        def result = runGradle('currentVersion')

        then:
        result.output.contains('Project version: 0.1.0-SNAPSHOT')
    }

    def "versionWithBranch should append branch name when not on release branch"() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionCreator('versionWithBranch')
                }
            }
        """

        when:
        checkout("test-branch")
        def result = runGradle('currentVersion')

        then:
        result.output.contains('Project version: 0.1.0-test-branch-SNAPSHOT')
    }

    def "versionWithBranch should not append branch name when on release branch"() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionCreator('versionWithBranch')
                }
            }
        """

        when:
        createTag('v1.0.0')
        def result = runGradle('currentVersion')

        then:
        result.output.contains('Project version: 1.0.0')
    }

    def "versionWithBranch should not append branch name when current ref is a version tag"() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionCreator('versionWithBranch')
                }
            }
        """

        when:
        createTag('v1.0.0')
        checkout('refs/tags/v1.0.0')
        def result = runGradle('currentVersion')

        then:
        result.output =~ /Project version: 1.0.0/
    }

    def "versionWithCommitHash should append hash when not on release branch"() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionCreator('versionWithCommitHash')
                }
            }
        """

        when:
        checkout("test-branch")
        def result = runGradle('currentVersion')

        then:
        result.output =~ /Project version: 0.1.0-[a-f0-9]{7}-SNAPSHOT/
    }

    def "versionWithCommitHash should not append hash on release branch"() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionCreator('versionWithCommitHash')
                }
            }
        """

        when:
        createTag('v1.0.0')
        def result = runGradle('currentVersion')

        then:
        result.output.contains('Project version: 1.0.0')
    }

    def "versionWithCommitHash should not append hash when current ref is a version tag"() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionCreator('versionWithCommitHash')
                }
            }
        """

        when:
        createTag('v1.0.0')
        checkout('refs/tags/v1.0.0')
        def result = runGradle('currentVersion')

        then:
        result.output =~ /Project version: 1.0.0/
    }

    def "versionWithCommitHash should not append when ref is a random tag"() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionCreator('versionWithCommitHash')
                }
            }
        """
        createTag('random-tag')
        checkout('refs/tags/random-tag')
        repository.commit(['.'], 'test-commit')

        when:
        def result = runGradle('currentVersion')

        then:
        result.output =~ /Project version: 0.1.0-SNAPSHOT/
    }

    def "versionWithBranch should not append branch name when HEAD on tag, but "() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionCreator('versionWithBranch')
                }
            }
        """
        checkout('feature/branch')
        createTag('v1.0.0')

        when:
        def result = runGradle('currentVersion')

        then:
        result.output.contains('Project version: 1.0.0-feature-branch')
    }

    def "versionWithCommitHash should not append hash when isTagRef is true HEAD on tag, but on branch"() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionCreator('versionWithCommitHash')
                }
            }
        """
        checkout('feature/branch')
        createTag('v1.0.0')

        when:
        def result = runGradle('currentVersion')

        then:
        result.output =~ /Project version: 1.0.0-[a-f0-9]{7}/
    }

    def "should fail with exception when trying to obtain undefined version creator"() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionCreator('unknown')
                }
            }
        """

        when:
        runGradle('currentVersion')

        then:
        def e = thrown(Exception)
        e.message.contains("There is no predefined version creator with unknown type.")
    }
}
