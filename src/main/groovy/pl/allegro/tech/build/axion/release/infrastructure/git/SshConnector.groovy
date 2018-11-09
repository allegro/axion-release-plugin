package pl.allegro.tech.build.axion.release.infrastructure.git

import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import org.eclipse.jgit.transport.JschConfigSessionFactory
import org.eclipse.jgit.transport.OpenSshConfig
import org.eclipse.jgit.util.FS
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity

class SshConnector extends JschConfigSessionFactory {

    private final ScmIdentity identity

    private JSch jsch

    SshConnector(ScmIdentity identity) {
        this.identity = identity
    }

    @Override
    protected void configure(OpenSshConfig.Host hc, Session session) {
        session.setConfig("StrictHostKeyChecking", "no")
    }

    @Override
    protected JSch getJSch(OpenSshConfig.Host hc, FS fs) throws JSchException {
        if (this.jsch == null) {
            this.jsch = (!identity.useDefault) ? createKeyBasedJSch() : createSshAgentBasedJSch(hc, fs)
        }

        return this.jsch
    }

    private JSch createKeyBasedJSch() {
        JSch jsch = new JSch()
        byte[] passPhrase = identity.passPhrase != null ? identity.passPhrase.bytes : null
        jsch.addIdentity('key', identity.privateKey.bytes, null, passPhrase)
        return jsch
    }

    private JSch createSshAgentBasedJSch(OpenSshConfig.Host hc, FS fs) {
        JSch ljsch = super.getJSch(hc, fs)

        if (!identity.disableAgentSupport) {
            SshAgentIdentityRepositoryFactory.tryToCreateIdentityRepository().ifPresent({
                r -> ljsch.setIdentityRepository(r)
            })
        }

        return ljsch
    }
}
