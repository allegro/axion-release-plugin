Authorization
=============

Interactive
-----------

All interactive authorization mechanisms are provided by `grgit <https://github.com/ajoberstar/grgit>`_,
see `authorization docs <http://ajoberstar.org/grgit/docs/groovydoc/org/ajoberstar/grgit/auth/AuthConfig.html>`_
for more information.

SSH Key/password
----------------

Interactive mode is fine for local development, but Continuous Integration servers authorize themselves mostly
using SSH keys. ``axion-release-plugin`` exposes options to use custom SSH keys (even password protected ones) when
pushing tags to remote.

Command line
^^^^^^^^^^^^

Use ``release.customKeyFile`` and ``release.customKeyPassword`` properties to force Git to use custom SSH keys to 
authorize in remote repository::


    ./gradlew release -Prelease.customKeyFile="./keys/secret_key_rsa" -Prelease.customKeyPassword=password

Dynamic
^^^^^^^

It might not be desirable to leave trace of key or password in CI server shell history. You can load credentials
dynamically using custom Gradle tasks right before release. To change credentials during Gradle build use
``scmVersion.repository.customKey`` and ``scmVersion.repository.customKeyPassword`` properties.

``scmVersion.repository.customKey`` accepts either ``File`` instance to read from or key in form of String.

::

    scmVersion {
        repository {
            customKey = project.file('keys/my_secret_key')
        }
    }
    
    
    task loadKeyPassword << {
        scmVersion.repository.customKeyPassword = loadPasswordFromSecureStorageOrSomething()
        // you can load the key from secure storage as well!
        scmVersion.repository.customKey = loadKeyFromSecureStorageOrSomething()
    }
    
    task release {
        dependsOn loadKeyPassword
    }
