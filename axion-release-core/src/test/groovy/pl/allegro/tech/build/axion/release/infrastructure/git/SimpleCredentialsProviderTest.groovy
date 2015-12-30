package pl.allegro.tech.build.axion.release.infrastructure.git

import org.eclipse.jgit.transport.CredentialItem
import spock.lang.Specification

class SimpleCredentialsProviderTest extends Specification {
    
    def "should provide username and password"() {
        given:
        SimpleCredentialsProvider provider = new SimpleCredentialsProvider('username', 'password')
        CredentialItem.Password passwordItem = new CredentialItem.Password('whatever')
        CredentialItem.Username usernameItem = new CredentialItem.Username()
        
        when:
        provider.get(null, passwordItem, usernameItem)

        then:
        usernameItem.value == 'username'
        passwordItem.value == 'password'.toCharArray()
    }
    
}
