package pl.allegro.tech.build.axion.release.domain.hooks

import spock.lang.Specification

class SimpleReleaseHookActionTest extends Specification {
    
    def "should run specified closure"() {
        given:
        boolean hookRun = false
        SimpleReleaseHookAction hook = new SimpleReleaseHookAction({ hookRun = true })
        
        when:
        hook.act(null)
        
        then:
        hookRun
    }
    
}
