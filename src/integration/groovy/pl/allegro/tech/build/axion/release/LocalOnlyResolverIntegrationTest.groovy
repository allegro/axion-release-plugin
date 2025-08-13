package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.TaskOutcome

class LocalOnlyResolverIntegrationTest extends BaseIntegrationTest {

    def "should not use localOnly when no project property or config flag is set"() {
        given:
        buildFile('''
            scmVersion {
                localOnly = false
            }

            task verifyLocalOnly {
                def scmVersion = project.scmVersion
                doLast {
                    println "localOnly: ${scmVersion.localOnly().get()}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyLocalOnly')

        then:
        result.task(":verifyLocalOnly").outcome == TaskOutcome.SUCCESS
        result.output.contains("localOnly: false")
    }

    def "should use localOnly when project release.localOnly property is present"() {
        given:
        buildFile('''
            scmVersion {
                localOnly = false
            }

            task verifyLocalOnly {
                def scmVersion = project.scmVersion
                doLast {
                    println "localOnly: ${scmVersion.localOnly().get()}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyLocalOnly', '-Prelease.localOnly')

        then:
        result.task(":verifyLocalOnly").outcome == TaskOutcome.SUCCESS
        result.output.contains("localOnly: true")
    }

    def "should use localOnly when config has localOnly flag set"() {
        given:
        buildFile('''
            scmVersion {
                localOnly = true
            }

            task verifyLocalOnly {
                def scmVersion = project.scmVersion
                doLast {
                    println "localOnly: ${scmVersion.localOnly().get()}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyLocalOnly')

        then:
        result.task(":verifyLocalOnly").outcome == TaskOutcome.SUCCESS
        result.output.contains("localOnly: true")
    }

    def "should override config with project property"() {
        given:
        buildFile('''
            scmVersion {
                localOnly = false
            }

            task verifyLocalOnly {
                def scmVersion = project.scmVersion
                doLast {
                    println "localOnly: ${scmVersion.localOnly().get()}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyLocalOnly', '-Prelease.localOnly')

        then:
        result.task(":verifyLocalOnly").outcome == TaskOutcome.SUCCESS
        result.output.contains("localOnly: true")
    }
}
