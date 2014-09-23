package pl.allegro.tech.build.axion.release.domain.scm

import groovy.transform.Immutable

@Immutable
public class ScmIdentity {

    final boolean useDefault

    final String privateKey

    final String passPhrase

    static ScmIdentity defaultIdentity() {
        return new ScmIdentity(useDefault: true)
    }

    static ScmIdentity customIdentity(String privateKey, String passPhrase) {
        return new ScmIdentity(privateKey: privateKey, passPhrase: passPhrase, useDefault: false)
    }
}