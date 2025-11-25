package pl.allegro.tech.build.axion.release.infrastructure.git;

import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

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
            String gitDir = normalizePath(repository.getDirectory().getAbsolutePath());
            Set<String> subsections = config.getSubsections("includeIf");
            return subsections.stream()
                .filter(condition -> matchesGitDir(condition, gitDir))
                .map(condition -> config.getString("includeIf", condition, "path"))
                .filter(this::isNotBlank)
                .peek(path -> logger.debug("Found includeIf config path: {}", path))
                .map(File::new)
                .filter(this::isReadableFile)
                .map(this::safeLoadConfig)
                .flatMap(Optional::stream)
                .map(this::extractCredentialsFromConfig)
                .flatMap(Optional::stream)
                .findFirst()
                .map(creds -> {
                    logger.debug("Successfully extracted credentials from includeIf config file");
                    return creds;
                });
        } catch (Exception e) {
            logger.debug("Error processing includeIf directives", e);
            return Optional.empty();
        }
    }

    private boolean matchesGitDir(String condition, String gitDir) {
        if (!condition.startsWith(GITDIR_PREFIX)) {
            return false;
        }
        String rawPattern = condition.substring(GITDIR_PREFIX.length()).trim();
        String pattern = stripTrailingSlash(normalizePath(rawPattern));
        return gitDir.equals(pattern) || gitDir.startsWith(pattern + "/");
    }

    private Optional<UsernamePassword> extractCredentialsFromConfig(Config config) {
        Set<String> httpSubsections = config.getSubsections("http");

        return httpSubsections.stream()
            .flatMap(subsection -> parseCredentialsFromHeaders(config, subsection))
            .findFirst();
    }

    private Stream<UsernamePassword> parseCredentialsFromHeaders(Config config, String subsection) {
        String[] extraHeaders = config.getStringList("http", subsection, "extraheader");
        return Arrays.stream(extraHeaders)
            .map(this::parseBasicAuthFromHeader)
            .flatMap(Optional::stream)
            .peek(up -> logger.debug("Found credentials in http.extraheader for subsection: {}", subsection));
    }

    private Optional<UsernamePassword> parseBasicAuthFromHeader(String header) {
        if (!isAuthorizationBasicHeader(header)) return Optional.empty();
        String base64Creds = header.substring(AUTH_HEADER_PREFIX.length()).trim();
        try {
            String decoded = new String(Base64.getDecoder().decode(base64Creds), StandardCharsets.UTF_8);
            int colonIndex = decoded.indexOf(':');
            if (colonIndex < 0) {
                return Optional.empty();
            }
            String username = decoded.substring(0, colonIndex);
            String password = decoded.substring(colonIndex + 1);
            return Optional.of(new UsernamePassword(username, password));
        } catch (IllegalArgumentException e) {
            logger.debug("Failed to decode base64 credentials", e);
            return Optional.empty();
        }
    }

    private boolean isAuthorizationBasicHeader(String header) {
        return header != null && header.regionMatches(true, 0, AUTH_HEADER_PREFIX, 0, AUTH_HEADER_PREFIX.length());
    }

    private String normalizePath(String path) {
        return path == null ? null : path.replace('\\', '/');
    }

    private String stripTrailingSlash(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    private Optional<FileBasedConfig> safeLoadConfig(File file) {
        FileBasedConfig includedConfig = new FileBasedConfig(file, FS.DETECTED);
        try {
            includedConfig.load();
            return Optional.of(includedConfig);
        } catch (IOException | ConfigInvalidException e) {
            logger.debug("Failed to load includeIf config file: {}", file.getPath(), e);
            return Optional.empty();
        }
    }

    private boolean isReadableFile(File file) {
        return file != null && file.exists() && file.isFile() && file.canRead();
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
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
