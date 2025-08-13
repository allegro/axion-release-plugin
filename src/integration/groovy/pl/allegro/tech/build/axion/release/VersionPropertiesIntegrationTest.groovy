package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.TaskOutcome

class VersionPropertiesIntegrationTest extends BaseIntegrationTest {

    def "should use default configuration when no project properties are present"() {
        given:
        buildFile('''
            scmVersion {
                sanitizeVersion = false
                ignoreUncommittedChanges = false
            }

            task verifyVersionProperties {
                def scmVersion = project.scmVersion
                doLast {
                    println "sanitizeVersion: ${scmVersion.sanitizeVersion.get()}"
                    println "ignoreUncommittedChanges: ${scmVersion.ignoreUncommittedChanges.get()}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyVersionProperties')

        then:
        result.task(":verifyVersionProperties").outcome == TaskOutcome.SUCCESS
        result.output.contains("sanitizeVersion: false")
        result.output.contains("ignoreUncommittedChanges: false")
    }

    def "should use version from 'release.version' property when present (no configuration-cache)"() {
        given:
        buildFile('''
            task verifyVersionProperties {
                doLast {
                    println "version: $version"
                }
            }
        ''')

        when:
        def result = runGradle('verifyVersionProperties', '-Prelease.version=1.0.0', '--no-configuration-cache')

        then:
        result.task(":verifyVersionProperties").outcome == TaskOutcome.SUCCESS
        result.output.contains("version: 1.0.0")
    }

    def "should use version from deprecated 'release.forceVersion' property when present  (no configuration-cache)"() {
        given:
        buildFile('''
            task verifyVersionProperties {
                doLast {
                    println "version: $version"
                }
            }
        ''')

        when:
        def result = runGradle('verifyVersionProperties', '-Prelease.forceVersion=1.0.0', '--no-configuration-cache')

        then:
        result.task(":verifyVersionProperties").outcome == TaskOutcome.SUCCESS
        result.output.contains("version: 1.0.0")
    }

    def "should use version from 'release.version' property when present that is supported by configuration-cache"() {
        given:
        buildFile('''
            task verifyVersionProperties {
                def scmVersion = project.scmVersion
                doLast {
                    println "version: $scmVersion.version"
                }
            }
        ''')

        when:
        def result = runGradle('verifyVersionProperties', '-Prelease.version=1.0.0')

        then:
        result.task(":verifyVersionProperties").outcome == TaskOutcome.SUCCESS
        result.output.contains("version: 1.0.0")
    }

    def "should set ignoreUncommittedChanges directly in configuration"() {
        given:
        buildFile('''
            scmVersion {
                ignoreUncommittedChanges = true
            }

            def scmVersion = project.scmVersion
            task verifyVersionProperties {
                doLast {
                    println "ignoreUncommittedChanges: ${scmVersion.ignoreUncommittedChanges.get()}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyVersionProperties')

        then:
        result.task(":verifyVersionProperties").outcome == TaskOutcome.SUCCESS
        result.output.contains("ignoreUncommittedChanges: true")
    }

    def "should use default version creator for master branch"() {
        given:
        buildFile('''
            scmVersion {
                versionCreator 'simple'
            }

            task verifyVersionCreator {
                def version = project.version
                doLast {
                    println "version: $version"
                }
            }
        ''')

        when:
        def result = runGradle('verifyVersionCreator')

        then:
        result.task(":verifyVersionCreator").outcome == TaskOutcome.SUCCESS
        result.output.contains("version:")
    }

    def "should use branch-specific version creator when branch matches pattern"() {
        given:
        buildFile('''
            scmVersion {
                branchVersionCreator = [
                    '.*': 'versionWithBranch'
                ]
            }

            task verifyVersionCreator {
                def version = project.version
                doLast {
                    println "version: $version"
                }
            }
        ''')

        when:
        def result = runGradle('verifyVersionCreator')

        then:
        result.task(":verifyVersionCreator").outcome == TaskOutcome.SUCCESS
        result.output.contains("version:")
    }

    def "should use version creator passed as command line option if present"() {
        given:
        buildFile('''
            scmVersion {
                versionCreator 'versionWithBranch'
            }

            task verifyVersionCreator {
                def version = project.version
                doLast {
                    println "version: $version"
                }
            }
        ''')

        when:
        def result = runGradle('verifyVersionCreator', '-Prelease.versionCreator=simple')

        then:
        result.task(":verifyVersionCreator").outcome == TaskOutcome.SUCCESS
        result.output.contains("version:")
    }
}
