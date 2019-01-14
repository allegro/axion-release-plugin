package pl.allegro.tech.build.axion.release.infrastructure.git;

import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.HttpTransport;
import org.eclipse.jgit.transport.NetRCCredentialsProvider;
import org.eclipse.jgit.transport.SshTransport;
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity;

class TransportConfigFactory {

    TransportConfigCallback create(ScmIdentity identity) {
        if (identity.isPrivateKeyBased()) {
            return createForSsh(identity);
        } else if (identity.isUsernameBased()) {
            return createForUsername(identity);
        } else if (identity.isUseDefault()) {
            return createForDefault(identity);
        }

        throw new IllegalArgumentException(
            "Transport callback can be created only for none (empty), private key or username based identity"
        );
    }

    private TransportConfigCallback createForDefault(ScmIdentity identity) {
        return transport -> {
            if (transport instanceof SshTransport) {
                SshTransport sshTransport = (SshTransport) transport;
                sshTransport.setSshSessionFactory(new SshConnector(identity));
            } else if (transport instanceof HttpTransport) {
                transport.setCredentialsProvider(new NetRCCredentialsProvider());
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
            transport.setCredentialsProvider(new SimpleCredentialsProvider(identity.getUsername(), identity.getPassword()));
        };
    }
}
