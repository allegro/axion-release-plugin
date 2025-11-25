package pl.allegro.tech.build.axion.release.infrastructure.git;

import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;

/**
 * Helper class to extract credentials from git config files, including support for
 * includeIf directives that JGit doesn't natively support.
 * <p>
 * This is specifically designed to work with actions/checkout@v6 which stores credentials
 * in a separate config file under $RUNNER_TEMP and references it via includeIf.gitdir directives.
 */
class GitConfigCredentialsHelper {
    private static final Logger logger = Logging.getLogger(GitConfigCredentialsHelper.class);
    private static final String GITDIR_PREFIX = "gitdir:";
    private static final String AUTH_HEADER_PREFIX = "Authorization: basic ";

    private final Repository repository;

    GitConfigCredentialsHelper(Repository repository) {
        this.repository = repository;
    }

    Optional<UsernamePassword> extractCredentials() {
        try {
            Config config = repository.getConfig();
            // Try to get credentials from includeIf directives or fallback to the main config.
            return extractCredentialsFromIncludeIf(config).or(() -> extractCredentialsFromConfig(config));
        } catch (Exception e) {
            logger.debug("Failed to extract credentials from git config", e);
            return Optional.empty();
        }
    }

    private Optional<UsernamePassword> extractCredentialsFromIncludeIf(Config config) {
        try {
            String gitDir = repository.getDirectory().getAbsolutePath().replace('\\', '/');
            Set<String> subsections = config.getSubsections("includeIf");
            for (String condition : subsections) {
                if (matchesGitDir(condition, gitDir)) {
                    String path = config.getString("includeIf", condition, "path");
                    if (path != null && !path.isEmpty()) {
                        logger.debug("Found includeIf config path: {}", path);
                        File configFile = new File(path);
                        if (configFile.exists() && configFile.canRead()) {
                            FileBasedConfig includedConfig = new FileBasedConfig(configFile, FS.DETECTED);
                            try {
                                includedConfig.load();
                                Optional<UsernamePassword> credentials = extractCredentialsFromConfig(includedConfig);
                                if (credentials.isPresent()) {
                                    logger.debug("Successfully extracted credentials from includeIf config file");
                                    return credentials;
                                }
                            } catch (IOException e) {
                                logger.debug("Failed to load includeIf config file: {}", path, e);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Error processing includeIf directives", e);
        }
        return Optional.empty();
    }

    /**
     * Checks if an includeIf condition matches the given git directory.
     * Supports the gitdir: condition format.
     */
    private boolean matchesGitDir(String condition, String gitDir) {
        if (condition.startsWith(GITDIR_PREFIX)) {
            String pattern = condition.substring(GITDIR_PREFIX.length()).trim();
            pattern = pattern.replace('\\', '/');
            if (pattern.endsWith("/")) {
                pattern = pattern.substring(0, pattern.length() - 1);
            }
            return gitDir.equals(pattern) || gitDir.startsWith(pattern + "/");
        }

        return false;
    }

    private Optional<UsernamePassword> extractCredentialsFromConfig(Config config) {
        Set<String> httpSubsections = config.getSubsections("http");
        for (String subsection : httpSubsections) {
            String[] extraHeaders = config.getStringList("http", subsection, "extraheader");
            for (String header : extraHeaders) {
                String base64Creds = null;
                if (header.regionMatches(true, 0, AUTH_HEADER_PREFIX, 0, AUTH_HEADER_PREFIX.length())) {
                    base64Creds = header.substring(AUTH_HEADER_PREFIX.length()).trim();
                }
                if (base64Creds != null) {
                    try {
                        String decoded = new String(Base64.getDecoder().decode(base64Creds));
                        int colonIndex = decoded.indexOf(':');
                        if (colonIndex >= 0) {
                            String username = decoded.substring(0, colonIndex);
                            String password = decoded.substring(colonIndex + 1);
                            logger.debug("Found credentials in http.extraheader for subsection: {}", subsection);
                            return Optional.of(new UsernamePassword(username, password));
                        }
                    } catch (IllegalArgumentException e) {
                        logger.debug("Failed to decode base64 credentials", e);
                    }
                }
            }
        }

        return Optional.empty();
    }

    static class UsernamePassword {
        private final String username;
        private final String password;

        UsernamePassword(String username, String password) {
            this.username = username;
            this.password = password;
        }

        String getUsername() {
            return username;
        }

        String getPassword() {
            return password;
        }
    }
}
