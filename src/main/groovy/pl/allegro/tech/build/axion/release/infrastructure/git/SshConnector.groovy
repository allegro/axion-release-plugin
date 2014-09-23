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
        session.setConfig("StrictHostKeyChecking", "no");
    }

    @Override
    protected JSch getJSch(OpenSshConfig.Host hc, FS fs) throws JSchException {
        if(this.jsch == null) {
            this.jsch = createJSch()
        }

        return this.jsch
    }

    private JSch createJSch() {
        JSch jsch = new JSch()
        jsch.addIdentity('key', identity.privateKey.bytes, null, identity.passPhrase.bytes)

        return jsch
    }
}
