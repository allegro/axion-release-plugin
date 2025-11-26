package pl.allegro.tech.build.axion.release.infrastructure.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileBasedConfig
import org.eclipse.jgit.util.FS
import spock.lang.Specification

import java.nio.file.Files

class GitConfigCredentialsHelperTest extends Specification {

    File repoDir
    Git git
    Repository repository

    def setup() {
        repoDir = Files.createTempDirectory("axion-test").toFile()
        git = Git.init().setDirectory(repoDir).call()
        repository = git.getRepository()
    }

    def cleanup() {
        git?.close()
        repoDir?.deleteDir()
    }

    /**
     * Helper method to encode credentials to base64 format used by git config.
     * Example: encodeCredentials("x-access-token", "test-token") returns "eC1hY2Nlc3MtdG9rZW46dGVzdC10b2tlbg=="
     */
    private String encodeCredentials(String username, String password) {
        return Base64.getEncoder().encodeToString("${username}:${password}".bytes)
    }

    def "should extract credentials from includeIf config file (actions/checkout v6 style)"() {
        given: "a temporary credentials config file with auth token"
        File credentialsFile = File.createTempFile("git-credentials-", ".config")

        // Simulate actions/checkout v6 credentials config
        // Base64 encodes "x-access-token:test-token"
        String encodedCreds = encodeCredentials("x-access-token", "test-token")
        FileBasedConfig credConfig = new FileBasedConfig(credentialsFile, FS.DETECTED)
        credConfig.setString("http", "https://github.com/", "extraheader", "AUTHORIZATION: basic ${encodedCreds}")
        credConfig.save()

        and: "git config with includeIf directive pointing to credentials file"
        String gitDir = repository.getDirectory().getAbsolutePath().replace('\\', '/')
        FileBasedConfig config = repository.getConfig() as FileBasedConfig
        config.setString("includeIf", "gitdir:" + gitDir, "path", credentialsFile.getAbsolutePath())
        config.save()

        when: "extracting credentials"
        GitConfigCredentialsHelper helper = new GitConfigCredentialsHelper(repository)
        Optional<GitConfigCredentialsHelper.UsernamePassword> credentials = helper.extractCredentials()

        then: "credentials should be extracted successfully"
        credentials.isPresent()
        credentials.get().getUsername() == "x-access-token"
        credentials.get().getPassword() == "test-token"

        cleanup:
        credentialsFile?.delete()
    }

    def "should extract credentials from http.extraheader in main config (legacy style)"() {
        given: "git config with extraheader in main config"
        String encodedCreds = encodeCredentials("x-access-token", "legacy-token")
        FileBasedConfig config = repository.getConfig() as FileBasedConfig
        config.setString("http", "https://github.com/", "extraheader", "AUTHORIZATION: basic ${encodedCreds}")
        config.save()

        when: "extracting credentials"
        GitConfigCredentialsHelper helper = new GitConfigCredentialsHelper(repository)
        Optional<GitConfigCredentialsHelper.UsernamePassword> credentials = helper.extractCredentials()

        then: "credentials should be extracted successfully"
        credentials.isPresent()
        credentials.get().getUsername() == "x-access-token"
        credentials.get().getPassword() == "legacy-token"
    }

    def "should return empty when no credentials found"() {
        when: "extracting credentials from empty config"
        GitConfigCredentialsHelper helper = new GitConfigCredentialsHelper(repository)
        Optional<GitConfigCredentialsHelper.UsernamePassword> credentials = helper.extractCredentials()

        then: "no credentials should be found"
        !credentials.isPresent()
    }

    def "should handle includeIf with trailing slash in gitdir pattern"() {
        given: "a temporary credentials config file"
        File credentialsFile = File.createTempFile("git-credentials-", ".config")

        String encodedCreds = encodeCredentials("x-access-token", "test-token")
        FileBasedConfig credConfig = new FileBasedConfig(credentialsFile, FS.DETECTED)
        credConfig.setString("http", "https://github.com/", "extraheader", "AUTHORIZATION: basic ${encodedCreds}")
        credConfig.save()

        and: "git config with includeIf directive with trailing slash"
        String gitDir = repository.getDirectory().getAbsolutePath().replace('\\', '/')
        FileBasedConfig config = repository.getConfig() as FileBasedConfig
        config.setString("includeIf", "gitdir:" + gitDir + "/", "path", credentialsFile.getAbsolutePath())
        config.save()

        when: "extracting credentials"
        GitConfigCredentialsHelper helper = new GitConfigCredentialsHelper(repository)
        Optional<GitConfigCredentialsHelper.UsernamePassword> credentials = helper.extractCredentials()

        then: "credentials should be extracted successfully"
        credentials.isPresent()
        credentials.get().getUsername() == "x-access-token"
        credentials.get().getPassword() == "test-token"

        cleanup:
        credentialsFile?.delete()
    }

    def "should prioritize includeIf credentials over main config credentials"() {
        given: "credentials in both includeIf file and main config"
        File credentialsFile = File.createTempFile("git-credentials-", ".config")

        // includeIf file with higher priority token
        String encodedPriorityCreds = encodeCredentials("x-access-token", "priority-token")
        FileBasedConfig credConfig = new FileBasedConfig(credentialsFile, FS.DETECTED)
        credConfig.setString("http", "https://github.com/", "extraheader", "AUTHORIZATION: basic ${encodedPriorityCreds}")
        credConfig.save()

        // Main config with different token
        String encodedMainCreds = encodeCredentials("x-access-token", "main-token")
        String gitDir = repository.getDirectory().getAbsolutePath().replace('\\', '/')
        FileBasedConfig config = repository.getConfig() as FileBasedConfig
        config.setString("includeIf", "gitdir:" + gitDir, "path", credentialsFile.getAbsolutePath())
        config.setString("http", "https://github.com/", "extraheader", "AUTHORIZATION: basic ${encodedMainCreds}")
        config.save()

        when: "extracting credentials"
        GitConfigCredentialsHelper helper = new GitConfigCredentialsHelper(repository)
        Optional<GitConfigCredentialsHelper.UsernamePassword> credentials = helper.extractCredentials()

        then: "includeIf credentials should be used"
        credentials.isPresent()
        credentials.get().getUsername() == "x-access-token"
        credentials.get().getPassword() == "priority-token"

        cleanup:
        credentialsFile?.delete()
    }

    def "should handle non-existent includeIf file gracefully"() {
        given: "git config with includeIf pointing to non-existent file"
        String gitDir = repository.getDirectory().getAbsolutePath().replace('\\', '/')
        FileBasedConfig config = repository.getConfig() as FileBasedConfig
        config.setString("includeIf", "gitdir:" + gitDir, "path", "/non/existent/file.config")
        config.save()

        when: "extracting credentials"
        GitConfigCredentialsHelper helper = new GitConfigCredentialsHelper(repository)
        Optional<GitConfigCredentialsHelper.UsernamePassword> credentials = helper.extractCredentials()

        then: "should return empty without throwing exception"
        !credentials.isPresent()
    }

    def "should handle Authorization header with lowercase 'a'"() {
        given: "git config with Authorization header (lowercase)"
        String encodedCreds = encodeCredentials("x-access-token", "test-token")
        FileBasedConfig config = repository.getConfig() as FileBasedConfig
        config.setString("http", "https://github.com/", "extraheader", "authorization: basic ${encodedCreds}")
        config.save()

        when: "extracting credentials"
        GitConfigCredentialsHelper helper = new GitConfigCredentialsHelper(repository)
        Optional<GitConfigCredentialsHelper.UsernamePassword> credentials = helper.extractCredentials()

        then: "credentials should be extracted successfully"
        credentials.isPresent()
        credentials.get().getUsername() == "x-access-token"
        credentials.get().getPassword() == "test-token"
    }
}
