package pl.allegro.tech.build.axion.release.domain.hooks

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Specification

class HooksConfigTest extends Specification {

    HookContext context;

    def setup() {
        context = new HookContext(
            null,
            null,
            null,
            new ScmPosition("longrev", "shortrev", "brnach"),
            Version.parse("0.1.0"),
            Version.parse("0.1.1")
        )
    }

    def "should cast a version-and-position closure to a CustomAction instance"() {
        given:
        def c = { v, p -> "$v:$p" }

        when:
        def result = HooksConfig.safeCastToCustomAction(c).apply(context)

        then:
        result == "${context.currentVersion}:${context.position}"
    }

    def "should cast a context closure without any type declared to a CustomAction instance"() {
        given:
        def c = { c -> "${c.currentVersion}:${c.position}" }

        when:
        def result = HooksConfig.safeCastToCustomAction(c).apply(context)

        then:
        result == "${context.currentVersion}:${context.position}"
    }

}
