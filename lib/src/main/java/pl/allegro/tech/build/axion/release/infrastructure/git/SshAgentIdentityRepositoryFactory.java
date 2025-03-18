package pl.allegro.tech.build.axion.release.infrastructure.git;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import com.jcraft.jsch.AgentConnector;
import com.jcraft.jsch.AgentIdentityRepository;
import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JUnixSocketFactory;
import com.jcraft.jsch.PageantConnector;
import com.jcraft.jsch.SSHAgentConnector;
import com.jcraft.jsch.UnixDomainSocketFactory;

class SshAgentIdentityRepositoryFactory {

    private static final Logger logger = Logging.getLogger(SshAgentIdentityRepositoryFactory.class);

    private SshAgentIdentityRepositoryFactory() {
        // static access only
    }

    static Optional<IdentityRepository> tryToCreateIdentityRepository() {
        Optional<AgentConnector> connector = trySshAgent().or(SshAgentIdentityRepositoryFactory::tryPageant);
        Optional<IdentityRepository> optionalIdentityRepository = connector.map(AgentIdentityRepository::new);
        optionalIdentityRepository.ifPresentOrElse(repository -> logger.info("Connected to SSH agent with status: " + statusDescription(repository.getStatus())),
                () -> logger.warn("No SSH Agent connection could be created. See debug log for details"));
        return optionalIdentityRepository;
    }

    private static Optional<AgentConnector> tryPageant() {
        try {
            return Optional.of(new PageantConnector());
        } catch (Exception e) {
            logger.info("Failed to use pageant as identity provider - " + e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug(stacktrace(e));
            }
        }

        return Optional.empty();
    }

    private static Optional<AgentConnector> trySshAgent() {
        try {
            JUnixSocketFactory jUnixSocketFactory = new JUnixSocketFactory();
            SSHAgentConnector sshAgentConnector = new SSHAgentConnector(jUnixSocketFactory);
            if (sshAgentConnector.isAvailable()) {
                return Optional.of(sshAgentConnector);
            }
        } catch (Exception e) {
            logger.info("Failed to connect to JUnix Socket ssh-agent - " + e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug(stacktrace(e));
            }
        }

        try {
            UnixDomainSocketFactory unixDomainSocketFactory = new UnixDomainSocketFactory();
            SSHAgentConnector sshAgentConnector = new SSHAgentConnector(unixDomainSocketFactory);
            if (sshAgentConnector.isAvailable()) {
                return Optional.of(sshAgentConnector);
            }
        } catch (Exception e) {
            logger.info("Failed to connect to Unix Domain Socket ssh-agent - " + e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug(stacktrace(e));
            }
        }

        return Optional.empty();
    }

    private static String stacktrace(Throwable e) {
        StringWriter writer = new StringWriter();
        try (PrintWriter pw = new PrintWriter(writer)) {
            e.printStackTrace(pw);
            return writer.toString();
        }
    }

    private static String statusDescription(int statusCode) {
        if (statusCode == IdentityRepository.NOTRUNNING) {
            return "Not Running";
        } else if (statusCode == IdentityRepository.UNAVAILABLE) {
            return "Unavailable";
        } else if (statusCode == IdentityRepository.RUNNING) {
            return "Running";
        } else {
            return "Unknown " + statusCode;
        }
    }
}
