package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Ignore

class ScmPropertiesIntegrationTest extends BaseIntegrationTest {

    def "should use remote name and tagsOnly option from configuration when no flags on project"() {
        given:
        buildFile('''
            scmVersion {
                repository {
                    remote = 'someRemote'
                    pushTagsOnly = true
                }
            }

            task verifyScmProperties {
                doLast {
                    println "remote: ${scmVersion.repository.remote.get()}"
                    println "pushTagsOnly: ${scmVersion.repository.pushTagsOnly.get()}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyScmProperties')

        then:
        result.task(":verifyScmProperties").outcome == TaskOutcome.SUCCESS
        result.output.contains("remote: someRemote")
        result.output.contains("pushTagsOnly: true")
    }

    def "should use default values when not specified in configuration"() {
        given:
        buildFile('''
            task verifyScmProperties {
                doLast {
                    println "remote: ${scmVersion.repository.remote.get()}"
                    println "pushTagsOnly: ${scmVersion.repository.pushTagsOnly.get()}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyScmProperties')

        then:
        result.task(":verifyScmProperties").outcome == TaskOutcome.SUCCESS
        result.output.contains("remote: origin")
        result.output.contains("pushTagsOnly: false")
    }

    def "should enable pushTagsOnly when set to true in configuration"() {
        given:
        buildFile('''
            scmVersion {
                repository {
                    pushTagsOnly = true
                }
            }

            task verifyScmProperties {
                doLast {
                    println "pushTagsOnly: ${scmVersion.repository.pushTagsOnly.get()}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyScmProperties')

        then:
        result.task(":verifyScmProperties").outcome == TaskOutcome.SUCCESS
        result.output.contains("pushTagsOnly: true")
    }

    @Ignore
    def "should override configuration with project property"() {
        given:
        buildFile('''
            scmVersion {
                repository {
                    pushTagsOnly = false
                }
            }

            task verifyScmProperties {
                doLast {
                    println "pushTagsOnly: ${scmVersion.repository.pushTagsOnly.get()}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyScmProperties', '-Prelease.pushTagsOnly')

        then:
        result.task(":verifyScmProperties").outcome == TaskOutcome.SUCCESS
        result.output.contains("pushTagsOnly: true")
    }
}
