package pl.allegro.tech.build.axion.release.domain.hooks

import spock.lang.Specification

class FileUpdateHookTest extends Specification {
    
    def "should update file by executing given regexp"() {
        given:
        File tmp = File.createTempFile("axion-release",".tmp")
        tmp.write("Hello\nthis is axion-release test\nversion: 1.0.0")
        
        FileUpdateHook hook = new FileUpdateHook([file: tmp, pattern: {p, v -> /(?m)^(version:) $v$/}, replacement: {p, v -> "\$1 $v"}])
        HookContext context = new HookContextBuilder(previousVersion: '1.0.0', currentVersion: '2.0.0').build()
        
        when:
        hook.act(context)
        
        then:
        tmp.text == "Hello\nthis is axion-release test\nversion: 2.0.0"
    }
    
}
