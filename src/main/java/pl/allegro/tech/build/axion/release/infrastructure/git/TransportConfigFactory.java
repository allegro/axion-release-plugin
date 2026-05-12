package pl.allegro.tech.build.axion.release.infrastructure.git;

import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.HttpTransport;
import org.eclipse.jgit.transport.NetRCCredentialsProvider;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity;

import java.util.Optional;

class TransportConfigFactory {

    TransportConfigCallback create(ScmIdentity identity, Repository repository) {
        if (identity.isPrivateKeyBased()) {
            return createForSsh(identity);
        } else if (identity.isUsernameBased()) {
            return createForUsername(identity);
        } else if (identity.isUseDefault()) {
            return createForDefault(identity, repository);
        }

        throw new IllegalArgumentException(
            "Transport callback can be created only for none (empty), private key or username based identity"
        );
    }

    private TransportConfigCallback createForDefault(ScmIdentity identity, Repository repository) {
        return transport -> {
            if (transport instanceof SshTransport) {
                SshTransport sshTransport = (SshTransport) transport;
                sshTransport.setSshSessionFactory(new SshConnector(identity));
            } else if (transport instanceof HttpTransport) {
                // Try to get credentials from git config (including includeIf files)
                // This supports actions/checkout@v6 which stores credentials in a separate file
                GitConfigCredentialsHelper credentialsHelper = new GitConfigCredentialsHelper(repository);
                Optional<GitConfigCredentialsHelper.UsernamePassword> credentials = credentialsHelper.extractCredentials();
                
                if (credentials.isPresent()) {
                    transport.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
                        credentials.get().getUsername(),
                        credentials.get().getPassword()
                    ));
                } else {
                    // Fallback to NetRC
                    transport.setCredentialsProvider(new NetRCCredentialsProvider());
                }
            }
        };
    }

    private TransportConfigCallback createForSsh(ScmIdentity identity) {
        return transport -> {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(new SshConnector(identity));
        };
    }

    private TransportConfigCallback createForUsername(ScmIdentity identity) {
        return transport -> {
            transport.setCredentialsProvider(new UsernamePasswordCredentialsProvider(identity.getUsername(), identity.getPassword()));
        };
    }
}
