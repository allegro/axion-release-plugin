package pl.allegro.tech.build.axion.release.infrastructure.git

import org.eclipse.jgit.errors.UnsupportedCredentialItem
import org.eclipse.jgit.transport.CredentialItem
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.URIish

class SimpleCredentialsProvider extends CredentialsProvider {
    
    private final String username
    
    private final String password

    SimpleCredentialsProvider(String username, String password) {
        this.username = username
        this.password = password
    }

    @Override
    boolean isInteractive() {
        return false
    }

    @Override
    boolean supports(CredentialItem... items) {
        items.every { it instanceof CredentialItem.Username || it instanceof CredentialItem.Password }
    }

    @Override
    boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
        items.each {
            if(it instanceof CredentialItem.Username) {
                it.setValue(username)
            }
            else if (it instanceof CredentialItem.Password) {
                it.setValue(password.toCharArray())
            }
        }
    }
}
