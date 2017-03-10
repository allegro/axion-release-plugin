Configuration overview
======================

All ``axion-release-plugin`` configuration options:

.. parsed-literal::

    scmVersion {

        repository { // :doc:`repository`
            type = 'git' // type of repository
            directory = project.rootProject.file('./') // repository location
            remote = 'origin' // remote name
    
            // :doc:`authorization`
            customKey = 'AAasaDDSSD...' or project.file('myKey') // custom authorization key (file or String)
            customKeyPassword = 'secret' // key password

            tagSelector = { tags -> ... } // choose tag to process when multiple on commit
        }

        // :doc:`dry_run`
        localOnly = false // never connect to remote
        
        // :doc:`uncommitted_changes`
        ignoreUncommittedChanges = true // should uncommitted changes force version bump
        
        // :doc`use_highest_version`
        useHighestVersion = false // Defaults as false, setting to true will find the highest visible version in the commit tree
    
        // :ref:`version-sanitization`
        sanitizeVersion = true // should created version be sanitized, true by default
    
        tag { // :ref:`version-parsing`
            prefix = 'tag-prefix' // prefix to be used, 'release' by default
            branchPrefix = [ // set different prefix per branch
                'legacy/.*' : 'legacy'
            ]

            versionSeparator = '-' // separator between prefix and version number, '-' by default
            serialize = { tag, version -> ... } // creates tag name from raw version
            deserialize = { tag, position, tagName -> ... } // reads raw version from tag
            initialVersion = { tag, position -> ... } // returns initial version if none found, 0.1.0 by default
        }
    
        nextVersion { // :doc:`next_version`
            suffix = 'alpha' // tag suffix
            separator = '-' // separator between version and suffix
            serializer = { nextVersionConfig, version -> ... } // append suffix to version tag
            deserializer = { nextVersionConfig, position -> ... } // strip suffix off version tag
        }

        // :ref:`version-decorating`
        versionCreator { version, position -> ... } // creates version visible for Gradle from raw version and current position in scm
        versionCreator 'versionWithBranch' // use one of predefined version creators
        branchVersionCreator = [ // use different creator per branch
            'feature/.*': 'default'
        ]

        // :ref:`version-incrementing`
        versionIncrementer {context, config -> ...} // closure that increments a version from the raw version, current position in scm and config
        versionIncrementer 'incrementPatch' // use one of predefined version incrementing rules
        branchVersionIncrementer = [ // use different incrementer per branch
            'feature/.*': 'incrementMinor'
        ]

        // :doc:`hooks`
        createReleaseCommit true // should create empty commit to annotate release in commit history, false by default
        releaseCommitMessage { version, position -> ... } // custom commit message if commits are created
    
        // :doc:`checks`
        checks {
            uncommittedChanges = false // permanently disable uncommitted changes check
            aheadOfRemote = false // permanently disable ahead of remote check
        }
    }

