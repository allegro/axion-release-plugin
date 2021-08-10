package pl.allegro.tech.build.axion.release.infrastructure.git;

import com.jcraft.jsch.JSch;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.git.transport.GitSshdSessionFactory;
import org.eclipse.jgit.transport.SshSessionFactory;
import pl.allegro.tech.build.axion.release.domain.scm.ScmIdentity;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

class SshConnector {

    static SshSessionFactory from(ScmIdentity identity) {
        JSch.setConfig("StrictHostKeyChecking", "no");
        SshClient client = SshClient.setUpDefaultClient();

        if (identity.isDisableAgentSupport()) {
            System.out.println("how to disable ssh-agent here?");
        }

        if (identity.isUsernameBased()) {
            client.addPasswordIdentity(identity.getPassword());
        } else if (identity.isPrivateKeyBased()) {
            client.addPublicKeyIdentity(getKeyPair(identity.getPrivateKey()));
        }

        return new GitSshdSessionFactory(client);
    }

    private static KeyPair getKeyPair(String privateKey) {
        try {
            String key = privateKey
                .replaceAll("\\n", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "");
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key));
            return new KeyPair(keyFactory.generatePublic(keySpecPKCS8), keyFactory.generatePrivate(keySpecPKCS8));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not parse private key", e);
        }
    }
}
