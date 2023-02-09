package pl.allegro.tech.build.axion.release.infrastructure.git;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity;

import static java.nio.charset.StandardCharsets.UTF_8;

class SshConnector extends JschConfigSessionFactory {

    private final ScmIdentity identity;

    private JSch jsch;

    SshConnector(ScmIdentity identity) {
        this.identity = identity;
    }

    @Override
    protected void configure(OpenSshConfig.Host hc, Session session) {
        session.setConfig("StrictHostKeyChecking", "no");
    }

    @Override
    protected JSch getJSch(OpenSshConfig.Host hc, FS fs) throws JSchException {
        if (this.jsch == null) {
            this.jsch = (!identity.isUseDefault()) ? createKeyBasedJSch() : createSshAgentBasedJSch(hc, fs);
        }

        return this.jsch;
    }

    private JSch createKeyBasedJSch() throws JSchException {
        JSch jsch = new JSch();
        byte[] passPhrase = identity.getPassPhrase() != null ? identity.getPassPhrase().getBytes(UTF_8) : null;
        jsch.addIdentity("key", identity.getPrivateKey().getBytes(UTF_8), null, passPhrase);
        return jsch;
    }

    private JSch createSshAgentBasedJSch(OpenSshConfig.Host hc, FS fs) throws JSchException {
        JSch ljsch = super.getJSch(hc, fs);

        boolean sshAgentEnabled = !identity.isDisableAgentSupport();
        if (sshAgentEnabled) {
            SshAgentIdentityRepositoryFactory.tryToCreateIdentityRepository().ifPresent(
                r -> ljsch.setIdentityRepository(r)
            );
        }

        return ljsch;
    }
}
