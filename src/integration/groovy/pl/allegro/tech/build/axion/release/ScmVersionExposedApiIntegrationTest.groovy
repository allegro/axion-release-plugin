package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.TaskOutcome
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

class ScmVersionExposedApiIntegrationTest extends BaseIntegrationTest {

    def "should return version information"() {
        given:
        buildFile("""
        task outputDecorated { doLast {
            println "Version: \${scmVersion.version}"
            println "Previous: \${scmVersion.previousVersion}" //'previousVersion' property smoke test
        } }
        """)

        when:
        def result = runGradle('outputDecorated')

        then:
        result.output.contains('Version: 0.1.0-SNAPSHOT')
        result.output.contains('Previous: 0.1.0')
        result.task(":outputDecorated").outcome == TaskOutcome.SUCCESS
    }

    def "should return version before any decorations at scmVersion.undecoratedVersion"() {
        given:
        buildFile("""
        task outputUndecorated { doLast {
            println "Undecorated: \${scmVersion.undecoratedVersion}"
        } }
        """)

        when:
        def result = runGradle('outputUndecorated')

        then:
        result.output.contains('Undecorated: 0.1.0')
        result.task(":outputUndecorated").outcome == TaskOutcome.SUCCESS
    }

    def "should return scm position from which version was read at scmVersion.scmPosition"() {
        given:
        buildFile("""
        task outputPosition { doLast {
            println "Revision: \${scmVersion.scmPosition.revision}"
            println "Short revision: \${scmVersion.scmPosition.shortRevision}"
            println "Branch: \${scmVersion.scmPosition.branch}"
        } }
        """)

        ScmPosition position = repository.currentPosition()

        when:
        def result = runGradle('outputPosition')

        then:
        result.output.contains("Revision: ${position.revision}")
        result.output.contains("Short revision: ${position.shortRevision}")
        result.output.contains("Branch: ${position.branch}")
        result.task(":outputPosition").outcome == TaskOutcome.SUCCESS
    }
}
