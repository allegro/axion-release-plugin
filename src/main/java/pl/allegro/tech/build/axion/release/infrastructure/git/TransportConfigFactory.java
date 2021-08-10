package pl.allegro.tech.build.axion.release.infrastructure.git;

import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.HttpTransport;
import org.eclipse.jgit.transport.NetRCCredentialsProvider;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity;

class TransportConfigFactory {

    TransportConfigCallback create(ScmIdentity identity) {
        if (identity.isPrivateKeyBased() || identity.isUseDefault()) {
            return createForSsh(identity);
        } else if (identity.isUsernameBased()) {
            return createForUsername(identity);
        }

        throw new IllegalArgumentException(
            "Transport callback can be created only for none (empty), private key or username based identity"
        );
    }

    private TransportConfigCallback createForSsh(ScmIdentity identity) {
        return transport -> {
            if (transport instanceof SshTransport) {
                SshTransport sshTransport = (SshTransport) transport;
                sshTransport.setSshSessionFactory(SshConnector.from(identity));
            } else if (transport instanceof HttpTransport) {
                transport.setCredentialsProvider(new NetRCCredentialsProvider());
            }
        };
    }

    private TransportConfigCallback createForUsername(ScmIdentity identity) {
        return transport -> {
            transport.setCredentialsProvider(new UsernamePasswordCredentialsProvider(identity.getUsername(), identity.getPassword()));
        };
    }
}
