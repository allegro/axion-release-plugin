package pl.allegro.tech.build.axion.release.infrastructure.git;

import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;
import com.jcraft.jsch.agentproxy.USocketFactory;
import com.jcraft.jsch.agentproxy.connector.PageantConnector;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;
import com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory;
import com.jcraft.jsch.agentproxy.usocket.NCUSocketFactory;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Content of this class is based on GrGit 2.x agent connector implementation.
 * TODO: add link to github
 * <p>
 * GrGit dropped support for JSch in favor of using native SSH command. However for the limited operations used by
 * axion-release-plugin, JSch seems to be sufficient.
 */
class SshAgentIdentityRepositoryFactory {

    private static final Logger logger = Logging.getLogger(SshAgentIdentityRepositoryFactory.class);

    static Optional<IdentityRepository> tryToCreateIdentityRepository() {
        Connector connector;
        IdentityRepository repository = null;

        logger.info("Trying to connect any to SSH agent for repository credentials");
        connector = trySshAgent();

        if (connector == null) {
            connector = tryPageant();
        }


        if (connector != null) {
            repository = createIdentityRepository(connector);
        }


        if (repository != null) {
            logger.info("Successfully connected to SSH agent and fetched identities, see debug logs for details");
        } else {
            logger.info("Failed to connect to SSH agent, see debug logs for details");
        }


        return Optional.ofNullable(repository);
    }

    private static IdentityRepository createIdentityRepository(Connector connector) {
        IdentityRepository repository = new RemoteIdentityRepository(connector);

        try {
            if (!((RemoteIdentityRepository) repository).getIdentities().isEmpty()) {
                return repository;
            } else {
                logger.debug("SSH agent holds no identities, not going to use it");
                return null;
            }

        } catch (Throwable e) {
            logger.warn("Failed to fetch identities from SSH agent, see debug logs for details");
            logger.debug(stacktrace(e));
        }


        return null;
    }

    private static Connector trySshAgent() {
        Connector connector = null;
        if (SSHAgentConnector.isConnectorAvailable()) {
            logger.debug("Found ssh-agent, trying to connect");

            Optional<USocketFactory> socketFactory = tryToCreateSocketFactory();
            if (socketFactory.isPresent()) {
                logger.debug("Connected to ssh-agent, using it as identity provider");
                try {
                    connector = new SSHAgentConnector(socketFactory.get());
                } catch (Throwable e) {
                    logger.warn("Failed to use ssh-agent as identity provider, see debug logs for details");
                    logger.debug(stacktrace(e));
                }

            } else {
                logger.warn("ssh-agent detected, but failed to connect, see debug logs for details");
            }

        }

        return connector;
    }

    private static Connector tryPageant() {
        Connector connector = null;
        if (PageantConnector.isConnectorAvailable()) {
            logger.debug("Found pageant, trying to connect");
            try {
                connector = new PageantConnector();
            } catch (Throwable e) {
                logger.warn("Failed to use pageant as identity provider, see debug logs for details");
                logger.debug(stacktrace(e));
            }

        }

        return connector;
    }

    private static Optional<USocketFactory> tryToCreateSocketFactory() {
        USocketFactory factory = null;
        Throwable exception = null;
        try {
            factory = new JNAUSocketFactory();
        } catch (Throwable e) {
            exception = e;
        }


        if (factory == null) {
            try {
                factory = new NCUSocketFactory();
            } catch (Throwable e) {
                exception = e;
            }

        }

        if (factory == null) {
            logger.warn("Failed to connect to ssh-agent, see debug logs for details");
            logger.debug(stacktrace(exception));
        }

        return Optional.ofNullable(factory);
    }

    private static String stacktrace(Throwable e) {
        StringWriter writer = new StringWriter();
        try (PrintWriter pw = new PrintWriter(writer)) {
            e.printStackTrace(pw);
            return writer.toString();
        }
    }
}
