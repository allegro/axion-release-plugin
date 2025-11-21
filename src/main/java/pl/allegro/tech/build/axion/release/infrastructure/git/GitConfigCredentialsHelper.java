package pl.allegro.tech.build.axion.release.infrastructure.git;

import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.SystemReader;
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
    
    private final Repository repository;
    
    GitConfigCredentialsHelper(Repository repository) {
        this.repository = repository;
    }
    
    /**
     * Attempts to extract credentials from git config, including credentials stored in
     * separate config files referenced by includeIf directives.
     * 
     * @return Optional containing username and password if credentials are found
     */
    Optional<UsernamePassword> extractCredentials() {
        try {
            Config config = repository.getConfig();
            
            // First, try to get credentials from includeIf directives
            Optional<UsernamePassword> credentialsFromInclude = extractCredentialsFromIncludeIf(config);
            if (credentialsFromInclude.isPresent()) {
                return credentialsFromInclude;
            }
            
            // Fallback: try to get credentials from the main config (legacy behavior)
            return extractCredentialsFromConfig(config);
        } catch (Exception e) {
            logger.debug("Failed to extract credentials from git config", e);
            return Optional.empty();
        }
    }
    
    /**
     * Extracts credentials from includeIf directives in the git config.
     * JGit doesn't support includeIf natively, so we manually parse and load the referenced files.
     */
    private Optional<UsernamePassword> extractCredentialsFromIncludeIf(Config config) {
        try {
            String gitDir = repository.getDirectory().getAbsolutePath().replace('\\', '/');
            
            // Get all includeIf subsections
            Set<String> subsections = config.getSubsections("includeIf");
            
            for (String condition : subsections) {
                // Check if this includeIf condition matches our git directory
                if (matchesGitDir(condition, gitDir)) {
                    String path = config.getString("includeIf", condition, "path");
                    if (path != null && !path.isEmpty()) {
                        logger.debug("Found includeIf config path: {}", path);
                        
                        // Load the referenced config file
                        File configFile = new File(path);
                        if (configFile.exists() && configFile.canRead()) {
                            FileBasedConfig includedConfig = new FileBasedConfig(configFile, FS.DETECTED);
                            try {
                                includedConfig.load();
                                
                                // Try to extract credentials from the included config
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
        if (condition.startsWith("gitdir:")) {
            String pattern = condition.substring("gitdir:".length()).trim();
            
            // Normalize paths - ensure both use forward slashes
            pattern = pattern.replace('\\', '/');
            
            // Remove trailing slash from pattern if present
            if (pattern.endsWith("/")) {
                pattern = pattern.substring(0, pattern.length() - 1);
            }
            
            // Check if gitDir matches the pattern
            // Support both exact match and pattern with trailing /
            return gitDir.equals(pattern) || gitDir.startsWith(pattern + "/");
        }
        
        return false;
    }
    
    /**
     * Extracts credentials from a git config object.
     * Looks for http.extraheader with Authorization header.
     */
    private Optional<UsernamePassword> extractCredentialsFromConfig(Config config) {
        // Try to get Authorization header from http.extraheader
        // actions/checkout sets: http.https://github.com/.extraheader = AUTHORIZATION: basic <base64>
        
        // Get all http subsections
        Set<String> httpSubsections = config.getSubsections("http");
        
        for (String subsection : httpSubsections) {
            String[] extraHeaders = config.getStringList("http", subsection, "extraheader");
            for (String header : extraHeaders) {
                if (header.startsWith("AUTHORIZATION: basic ") || header.startsWith("Authorization: basic ")) {
                    String base64Creds = header.substring(header.indexOf("basic ") + 6).trim();
                    try {
                        String decoded = new String(Base64.getDecoder().decode(base64Creds));
                        int colonIndex = decoded.indexOf(':');
                        if (colonIndex > 0) {
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
    
    /**
     * Simple container for username and password.
     */
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
