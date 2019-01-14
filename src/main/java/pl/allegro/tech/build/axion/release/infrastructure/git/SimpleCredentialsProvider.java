package pl.allegro.tech.build.axion.release.infrastructure.git;

import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

import java.util.Arrays;

class SimpleCredentialsProvider extends CredentialsProvider {

    private final String username;
    private final String password;

    SimpleCredentialsProvider(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean isInteractive() {
        return false;
    }

    @Override
    public boolean supports(CredentialItem... items) {
        return Arrays.stream(items).allMatch(i ->
            i instanceof CredentialItem.Username || i instanceof CredentialItem.Password
        );
    }

    @Override
    public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
        for (CredentialItem item : items) {
            if (item instanceof CredentialItem.Username) {
                ((CredentialItem.Username) item).setValue(username);
            } else if (item instanceof CredentialItem.Password) {
                ((CredentialItem.Password) item).setValue(password.toCharArray());
            }
        }
        return true;
    }
}
