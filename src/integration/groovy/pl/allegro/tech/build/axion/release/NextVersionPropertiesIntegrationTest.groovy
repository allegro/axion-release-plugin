package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.TaskOutcome

class NextVersionPropertiesIntegrationTest extends BaseIntegrationTest {

    def "should use default values when no project properties are present"() {
        given:
        buildFile('''
            scmVersion {
                nextVersion {
                    suffix = 'alpha'
                    separator = '-'
                }
            }

            task verifyNextVersion {
                def scmVersion = project.scmVersion
                doLast {
                    println "nextVersion: ${scmVersion.nextVersion.nextVersionProperties().nextVersion}"
                    println "suffix: ${scmVersion.nextVersion.nextVersionProperties().suffix}"
                    println "separator: ${scmVersion.nextVersion.nextVersionProperties().separator}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyNextVersion')

        then:
        result.task(":verifyNextVersion").outcome == TaskOutcome.SUCCESS
        result.output.contains("suffix: alpha")
        result.output.contains("separator: -")
        result.output.contains("nextVersion: null")
    }

    def "should read nextVersion from 'release.version' property"() {
        given:
        buildFile('''
            scmVersion {
                nextVersion {
                    suffix = 'alpha'
                    separator = '-'
                }
            }

            task verifyNextVersion {
                def scmVersion = project.scmVersion
                doLast {
                    println "nextVersion: ${scmVersion.nextVersion.nextVersionProperties().nextVersion}"
                    println "suffix: ${scmVersion.nextVersion.nextVersionProperties().suffix}"
                    println "separator: ${scmVersion.nextVersion.nextVersionProperties().separator}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyNextVersion', '-Prelease.version=1.0.0')

        then:
        result.task(":verifyNextVersion").outcome == TaskOutcome.SUCCESS
        result.output.contains("nextVersion: 1.0.0")
        result.output.contains("suffix: alpha")
        result.output.contains("separator: -")
    }

    def "should read nextVersion from deprecated 'release.nextVersion' property"() {
        given:
        buildFile('''
            scmVersion {
                nextVersion {
                    suffix = 'alpha'
                    separator = '-'
                }
            }

            task verifyNextVersion {
                def scmVersion = project.scmVersion
                doLast {
                    println "nextVersion: ${scmVersion.nextVersion.nextVersionProperties().nextVersion}"
                    println "suffix: ${scmVersion.nextVersion.nextVersionProperties().suffix}"
                    println "separator: ${scmVersion.nextVersion.nextVersionProperties().separator}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyNextVersion', '-Prelease.nextVersion=1.0.0')

        then:
        result.task(":verifyNextVersion").outcome == TaskOutcome.SUCCESS
        result.output.contains("nextVersion: 1.0.0")
        result.output.contains("suffix: alpha")
        result.output.contains("separator: -")
    }

    def "should override configuration values with project properties"() {
        given:
        buildFile('''
            scmVersion {
                nextVersion {
                    suffix = 'alpha'
                    separator = '-'
                }
            }

            task verifyNextVersion {
                def scmVersion = project.scmVersion
                doLast {
                    println "nextVersion: ${scmVersion.nextVersion.nextVersionProperties().nextVersion}"
                    println "suffix: ${scmVersion.nextVersion.nextVersionProperties().suffix}"
                    println "separator: ${scmVersion.nextVersion.nextVersionProperties().separator}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyNextVersion', '-Prelease.version=1.0.0')

        then:
        result.task(":verifyNextVersion").outcome == TaskOutcome.SUCCESS
        result.output.contains("nextVersion: 1.0.0")
        result.output.contains("suffix: alpha")
        result.output.contains("separator: -")
    }
}
