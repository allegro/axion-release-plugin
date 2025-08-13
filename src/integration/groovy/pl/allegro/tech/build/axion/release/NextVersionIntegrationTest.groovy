package pl.allegro.tech.build.axion.release

class NextVersionIntegrationTest extends BaseIntegrationTest {

    def "should fail when passing empty nextVersion.suffix"() {
        given:
        buildFile(
            """
scmVersion {
    nextVersion {
        suffix = ''
    }
}
"""
        )

        when:
        def result = runGradleAndFail('currentVersion')

        then:
        result.output.contains("scmVersion.nextVersion.suffix can't be empty!")
    }
}
