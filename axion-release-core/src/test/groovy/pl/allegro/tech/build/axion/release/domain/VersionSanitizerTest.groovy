package pl.allegro.tech.build.axion.release.domain

import spock.lang.Specification

class VersionSanitizerTest extends Specification {

    VersionSanitizer sanitizer = new VersionSanitizer();

    def "should sanitize version containing wrong characters"() {
        expect:
        sanitizer.sanitize('my-project-0.1.0-feature/123') == 'my-project-0.1.0-feature-123'
        sanitizer.sanitize('my-project-0.1.0-feature/123?hello') == 'my-project-0.1.0-feature-123-hello'
        sanitizer.sanitize('my-project-0.1.0-feature/123:hello') == 'my-project-0.1.0-feature-123-hello'
    }
}
