package pl.allegro.tech.build.axion.release.domain.hooks

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.VersionWithPosition
import pl.allegro.tech.build.axion.release.domain.properties.HooksProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Specification

class ReleaseHooksRunnerTest extends Specification {

    private ReleaseHooksRunner runner = new ReleaseHooksRunner(null, null)

    private VersionWithPosition version = new VersionWithPosition(
            new Version.Builder().setNormalVersion('2.0.0-SNAPSHOT').build(),
            new Version.Builder().setNormalVersion('1.0.0').build(),
            new ScmPosition('master', 'release-1.0.0', false, false)
    )

    private Version releaseVersion = new Version.Builder().setNormalVersion('2.0.0').build()

    def "should run all pre-release jobs"() {
        given:
        int preReleaseRun = 0
        Map versions = [:]

        HooksProperties rules = new HooksProperties([
                { c -> preReleaseRun++ } as ReleaseHookAction,
                { c -> preReleaseRun++; versions.previous = c.previousVersion; versions.current = c.releaseVersion }  as ReleaseHookAction
        ], [])

        when:
        runner.runPreReleaseHooks(rules, null, version, releaseVersion)

        then:
        preReleaseRun == 2
        versions == [previous: '1.0.0', current: '2.0.0']
    }

    def "should run all post-release jobs"() {
        given:
        int postReleaseRun = 0

        HooksProperties rules = new HooksProperties([], [
                {c -> postReleaseRun++ } as ReleaseHookAction,
                {c -> postReleaseRun++ } as ReleaseHookAction
        ])

        when:
        runner.runPostReleaseHooks(rules, null, version, version.version)

        then:
        postReleaseRun == 2
    }
}
