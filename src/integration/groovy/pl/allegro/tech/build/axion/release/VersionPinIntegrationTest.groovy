package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.TaskOutcome

class VersionPinIntegrationTest extends BaseIntegrationTest {

    def "should not have pin task if disabled"() {
        given:
        buildFile("""
            scmVersion {
                pinning {
                    enabled = false
                }
            }
        """)

        when:
        def result = runGradleAndFail('pinVersion')

        then:
        result.output.contains("Task 'pinVersion' not found")
    }

    def "should output pinned version"() {
        given:
        buildFile("""
            scmVersion {
                pinning {
                    enabled = true
                }
            }
        """)

        when:
        def result = runGradle('pinVersion')

        then:
        result.output.contains('Version pinned to: 0.1.0')
        result.task(":pinVersion").outcome == TaskOutcome.SUCCESS
    }

    def "should write current version to standard pinfile"() {
        given:
        buildFile("""
            scmVersion {
                pinning {
                    enabled = true
                }
            }
        """)

        when:
        def result = runGradle('pinVersion')

        then:
        result.output.contains('Version pinned to: 0.1.0')
        result.task(":pinVersion").outcome == TaskOutcome.SUCCESS
        new File(temporaryFolder, "pinned-version.json").exists()
    }

    def "should write current version to other pinfile"() {
        given:
        buildFile("""
            scmVersion {
                pinning {
                    enabled = true
                    pinFile.set(layout.projectDirectory.file("pin.file"))
                }
            }
        """)

        when:
        def result = runGradle('pinVersion')

        then:
        result.task(":pinVersion").outcome == TaskOutcome.SUCCESS
        !new File(temporaryFolder, "pinned-version.json").exists()
        new File(temporaryFolder, "pin.file").exists()
    }
}
