package pl.allegro.tech.build.axion.release.domain.hooks

import com.github.zafarkhaja.semver.Version
import spock.lang.Specification

class FileUpdateHookTest extends Specification {
    
    def "should update file by executing given regexp"() {
        given:
        File tmp = File.createTempFile("axion-release",".tmp")
        tmp.write("Hello\nthis is axion-release test\nversion: 1.0.0")
        
        FileUpdateHook hook = new FileUpdateHook([file: tmp, pattern: {p, v -> /(?m)^version: $v$/}, replacement: {p, v -> "version: $v"}])
        HookContext context = new HookContext(null, null, new Version.Builder().setNormalVersion("1.0.0").build(),
                new Version.Builder().setNormalVersion("2.0.0").build())
        
        when:
        hook.act(context)
        
        then:
        tmp.text == "Hello\nthis is axion-release test\nversion: 2.0.0"
    }
    
}
