package pl.allegro.tech.build.axion.release.domain.hooks

import spock.lang.Specification

class FileUpdateHookTest extends Specification {
    
    def "should update file by executing given regexp"() {
        given:
        File tmp = File.createTempFile("axion-release",".tmp")
        tmp.write("Hello\nthis is axion-release test\nversion: 1.0.0")
        
        FileUpdateHook hook = new FileUpdateHook([file: tmp, pattern: {v, p -> /(?m)^(version.) $v$/}, replacement: {v, p -> "\$1 $v"}])
        HookContext context = new HookContextBuilder(previousVersion: '1.0.0', currentVersion: '2.0.0').build()
        
        when:
        hook.act(context)
        
        then:
        tmp.text == "Hello\nthis is axion-release test\nversion: 2.0.0"
    }
    
    def "should update all listed files"() {
        given:
        File tmp1 = File.createTempFile("axion-release1",".tmp")
        tmp1.write("1.0.0")
        
        File tmp2 = File.createTempFile("axion-release2",".tmp")
        tmp2.write("1.0.0")

        FileUpdateHook hook = new FileUpdateHook([files: [tmp1, tmp2], pattern: {v, p -> /$v$/}, replacement: {v, p -> "$v"}])
        HookContext context = new HookContextBuilder(previousVersion: '1.0.0', currentVersion: '2.0.0').build()

        when:
        hook.act(context)

        then:
        tmp1.text == "2.0.0"
        tmp2.text == "2.0.0"
    }
}
