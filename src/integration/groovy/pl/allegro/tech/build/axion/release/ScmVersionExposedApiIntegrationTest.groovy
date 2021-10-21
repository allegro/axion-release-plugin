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

    def "getUncached should respect changing prefix"() {
        given:
        repository.tag("v1.2.3")
        repository.tag("prefix4.5.6")
        repository.tag("another7.8.9")
        buildFile("""
        task uncachedVersion { doLast {
            println "Default prefix: \${scmVersion.uncached.decoratedVersion}"
            scmVersion.tag.prefix = "prefix"
            println "Custom prefix 1: \${scmVersion.uncached.decoratedVersion}"
            scmVersion.tag.prefix = "another"
            println "Custom prefix 2: \${scmVersion.uncached.decoratedVersion}"
        } }
        """)

        when:
        def result = runGradle('uncachedVersion')

        then:
        result.output.contains('Default prefix: 1.2.3')
        result.output.contains('Custom prefix 1: 4.5.6')
        result.output.contains('Custom prefix 2: 7.8.9')
        result.task(":uncachedVersion").outcome == TaskOutcome.SUCCESS
    }

    def "getUncached should not modify cached version"() {
        given:
        repository.tag("v1.2.3")
        repository.tag("prefix4.5.6")
        buildFile("""
        task uncachedVersion { doLast {
            println "Default prefix: \${scmVersion.version}"
            scmVersion.tag.prefix = "prefix"
            println "Custom prefix: \${scmVersion.uncached.decoratedVersion}"
            scmVersion.tag.prefix = "another"
            println "Cached version: \${scmVersion.version}"
        } }
        """)

        when:
        def result = runGradle('uncachedVersion')

        then:
        result.output.contains('Default prefix: 1.2.3')
        result.output.contains('Custom prefix: 4.5.6')
        result.output.contains('Cached version: 1.2.3')
        result.task(":uncachedVersion").outcome == TaskOutcome.SUCCESS
    }
}
