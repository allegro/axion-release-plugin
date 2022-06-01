# Authorization

## SSH Agent

If no other options are used and repository requires authorization, `axion-release`
will try to fetch authorization options from SSH agent (`ssh-agent` on Linux and `pageant` on Windows)
or netrc for HTTP (`<user-home>/.netrc` on Linux and `<user-home>\_netrc>` on Windows).

All interaction with SSH agent is logged on info and debug levels.

## SSH Key/password

Interactive mode is fine for local development, but Continuous
Integration servers authorize themselves mostly using SSH keys.
`axion-release-plugin` exposes options to use custom SSH keys (even
password protected ones) when pushing tags to remote.

### Command line

Use `release.customKeyFile` and `release.customKeyPassword` properties
to force Git to use custom SSH keys to authorize in remote repository:

    ./gradlew release -Prelease.customKeyFile="./keys/secret_key_rsa" -Prelease.customKeyPassword=password

### Dynamic

It might not be desirable to leave trace of key or password in CI server
shell history. You can load credentials dynamically using custom Gradle
tasks right before release. To change credentials during Gradle build
use `scmVersion.repository.customKey` and
`scmVersion.repository.customKeyPassword` properties.

`scmVersion.repository.customKey` accepts either `File` instance to read
from or key in form of String.

    scmVersion {
        repository {
            customKeyFile.set(project.file('keys/my_secret_key'))
        }
    }


    task loadKeyPassword << {
        scmVersion.repository.customKeyPassword.set( loadPasswordFromSecureStorageOrSomething())
        // you can load the key from secure storage as well!
        scmVersion.repository.customKey.set( loadKeyFromSecureStorageOrSomething())
    }

    task release {
        dependsOn loadKeyPassword
    }

## HTTP Basic Auth/GitHub tokens

Same as with SSH keys, `axion-release-plugin`gives option to set basic
auth data when connecting to repository via HTTP. This is especially
useful when using [GitHub OAuth
tokens](https://help.github.com/articles/git-automation-with-oauth-tokens/).

### Command line

Use `release.customUsername` and `release.customPassword` properties to
set username and password that will be provided when using Basic Auth.
If password is not set, it defaults to empty string.

### Dynamic

Username and password can be provided in runtime, just before push is
made via `scmVersion.repository.customUsername` and
`scmVersion.repository.customPassword` properties:

    task loadGitHubToken << {
        scmVersion.repository.customUsername.set(loadGitHubTokenFromSomewhere())
    }
