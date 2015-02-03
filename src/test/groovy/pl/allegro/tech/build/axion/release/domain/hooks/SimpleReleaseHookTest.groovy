package pl.allegro.tech.build.axion.release.domain.hooks

import spock.lang.Specification

class SimpleReleaseHookTest extends Specification {
    
    def "should run specified closure"() {
        given:
        boolean hookRun = false
        SimpleReleaseHook hook = new SimpleReleaseHook({ hookRun = true })
        
        when:
        hook.act(null)
        
        then:
        hookRun
    }
    
}
