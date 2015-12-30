package pl.allegro.tech.build.axion.release.domain.scm

import groovy.transform.Immutable

@Immutable
public class ScmIdentity {

    final boolean useDefault

    final boolean privateKeyBased
    
    final boolean usernameBased
    
    final String privateKey

    final String passPhrase
    
    final String username
    
    final String password

    static ScmIdentity defaultIdentity() {
        return new ScmIdentity(useDefault: true)
    }

    static ScmIdentity keyIdentity(String privateKey, String passPhrase) {
        return new ScmIdentity(privateKey: privateKey, passPhrase: passPhrase, privateKeyBased: true, useDefault: false)
    }

    static ScmIdentity usernameIdentity(String username, String password) {
        return new ScmIdentity(username: username, password: password, usernameBased: true, useDefault: false)
    }
}