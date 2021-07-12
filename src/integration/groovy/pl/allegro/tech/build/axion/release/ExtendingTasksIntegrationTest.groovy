package pl.allegro.tech.build.axion.release

import spock.lang.Unroll

class ExtendingTasksIntegrationTest extends BaseIntegrationTest {

    @Unroll
    def "should allow on creating task that extends #task"() {
        given:
        vanillaBuildFile("""
            plugins {
                id 'pl.allegro.tech.build.axion-release' apply false
            }

            import pl.allegro.tech.build.axion.release.domain.VersionConfig
            import pl.allegro.tech.build.axion.release.${task}

            task customTask(type: ${task}) {
                versionConfig = new VersionConfig(project)
            }
            
            customTask.doFirst {
                println "Check for this message"
            }
        """)

        when:
        def result = runGradle('customTask', '-Prelease.dryRun', '-Prelease.disableChecks', '-Prelease.version=1.2.3', '-s')

        then:
        result.output.contains('Check for this message')

        where:
        task << [
            'CreateReleaseTask',
            'MarkNextVersionTask',
            'OutputCurrentVersionTask',
            'PushReleaseTask',
            'ReleaseTask',
            'VerifyReleaseTask'
        ]
    }

}
