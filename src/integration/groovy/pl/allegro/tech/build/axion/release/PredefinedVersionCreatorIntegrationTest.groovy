package pl.allegro.tech.build.axion.release

import spock.lang.Tag

class PredefinedVersionCreatorIntegrationTest extends BaseIntegrationTest {

    def setup() {
        generateGitIgnoreFile(temporaryFolder)
    }

    def "simple version creator should just return version string"() {
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
        result.output.contains('Project version: 0.1.0-SNAPSHOT\n')
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

        and:
        checkout("test-branch")

        when:
        def result = runGradle('currentVersion')

        then:
        result.output.contains('Project version: 0.1.0-test-branch-SNAPSHOT\n')
    }

    def "versionWithBranch should not append branch name and SNAPSHOT suffix when not on release branch but on version tag"() {
        given:
        buildFile """
            scmVersion {
                release {
                    versionCreator('versionWithBranch')
                }
            }
        """
        commit(['.'], "feature commit")

        and:
        createTag('v1.0.0')
        checkout("test-branch")

        when:
        def result = runGradle('currentVersion')

        then:
        result.output.contains('Project version: 1.0.0\n')
    }

    @Tag("monorepo")
    def "versionWithBranch should not append branch name and SNAPSHOT suffix when not on release branch but on version tag"() {
        given:
        vanillaSettingsFile("""
            rootProject.name = 'root-project'

            include 'sub-project-a'
            include 'sub-project-b'
            """
        )

        vanillaSubprojectBuildFile("sub-project-a", """
            plugins {
                id 'pl.allegro.tech.build.axion-release'
            }

            scmVersion {
                tag {
                    prefix = 'a-'
                }
            }
            """
        )

        vanillaSubprojectBuildFile("sub-project-b", """
            plugins {
                id 'pl.allegro.tech.build.axion-release'
            }

            scmVersion {
                tag {
                    prefix = 'b-'
                }
            }
            """
        )

        when:
        // initial state
        repository.commit(['.'], 'Before changes')
        createTag('a-1.0.2')
        createTag('b-2.0.3')
        checkout("test-branch")

        // modify only sub-project-a
        customProjectFile('sub-project-a/something.txt', 'Some content')
        repository.commit(['.'], 'Modify sub-project-a')

        def resultA = runGradle(':sub-project-a:currentVersion')
        def resultB = runGradle(':sub-project-b:currentVersion')

        then:
        resultA.output.contains('Project version: 1.0.3-test-branch-SNAPSHOT\n')
        resultB.output.contains('Project version: 2.0.3\n')
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

        and:
        createTag('v1.0.0')

        when:
        def result = runGradle('currentVersion')

        then:
        result.output.contains('Project version: 1.0.0\n')
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

        and:
        createTag('v1.0.0')
        checkout('refs/tags/v1.0.0')

        when:
        def result = runGradle('currentVersion')

        then:
        result.output.contains('Project version: 1.0.0\n')
    }

    def "versionWithBranch should append branch name when on version tag but with uncommitted changes and ignoreUncommittedChanges is set to false"() {
        given:
        buildFile """
            scmVersion {
                ignoreUncommittedChanges.set(false)
                release {
                    versionCreator('versionWithBranch')
                }
            }
        """
        commit(['.'], "feature commit")

        and:
        createTag('v1.0.0')
        checkout("test-branch")
        newFile('dirty.txt')

        when:
        def result = runGradle('currentVersion')

        then:
        result.output.contains('Project version: 1.0.1-test-branch-SNAPSHOT\n')
    }

    @Tag("POSSIBLE-BUG")
    def "versionWithBranch will append branch name when on version tag but with uncommitted changes and ignoreUncommittedChanges is set to default"() {
        given:
        buildFile """
            scmVersion {
                ignoreUncommittedChanges.set(true)
                release {
                    versionCreator('versionWithBranch')
                }
            }
        """
        commit(['.'], "feature commit")

        and:
        createTag('v1.0.0')
        checkout("test-branch")
        newFile('dirty.txt')

        when:
        def result = runGradle('currentVersion')

        then:
        result.output.contains('Project version: 1.0.0-test-branch\n')
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

        and:
        checkout("test-branch")

        when:
        def result = runGradle('currentVersion')

        then:
        result.output =~ /Project version: 0.1.0-[a-f0-9]{7}-SNAPSHOT\n/
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

        and:
        createTag('v1.0.0')

        when:
        def result = runGradle('currentVersion')

        then:
        result.output.contains('Project version: 1.0.0\n')
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

        and:
        createTag('v1.0.0')
        checkout('refs/tags/v1.0.0')

        when:
        def result = runGradle('currentVersion')

        then:
        result.output.contains('Project version: 1.0.0\n')
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
        commit(['.'], "feature commit")

        and:
        createTag('random-tag')
        checkout('refs/tags/random-tag')
        commit(['.'], 'test-commit')

        when:
        def result = runGradle('currentVersion')

        then:
        result.output.contains('Project version: 0.1.0-SNAPSHOT\n')
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

    void generateGitIgnoreFile(File dir) {
        File gitIgnore = new File(dir, ".gitignore")
        gitIgnore << """\
        .gradle
        build/
        """.stripIndent()
    }
}
