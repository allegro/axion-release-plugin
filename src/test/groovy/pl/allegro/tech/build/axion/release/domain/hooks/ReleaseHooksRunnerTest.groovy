package pl.allegro.tech.build.axion.release.domain.hooks

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.VersionWithPosition
import spock.lang.Specification

class ReleaseHooksRunnerTest extends Specification {
    
    private HooksConfig config = new HooksConfig()
    
    private ReleaseHooksRunner runner = new ReleaseHooksRunner(null, config)
    
    private VersionWithPosition version = VersionWithPosition.withoutPosition(new Version.Builder().setNormalVersion("1.0.0").build())
    
    def "should run all pre-release jobs"() {
        given:
        int preReleaseRun = 0
        config.pre { preReleaseRun++ }
        config.pre { preReleaseRun++ }

        when:
        runner.runPreReleaseHooks(version, version.version)
        
        then:
        preReleaseRun == 2
    }

    def "should run all post-release jobs"() {
        given:
        int postReleaseRun = 0
        config.post { postReleaseRun++ }
        config.post { postReleaseRun++ }

        when:
        runner.runPostReleaseHooks(version, version.version)

        then:
        postReleaseRun == 2
    }
}
