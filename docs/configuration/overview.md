# Configuration overview

All `axion-release-plugin` configuration options:


    scmVersion {

        repository {
            // doc: Repository
            type = 'git' // type of repository
            directory = project.rootProject.file('./') // repository location
            remote = 'origin' // remote name

            // doc: Authorization
            customKey = 'AAasaDDSSD...' or project.file('myKey') // custom authorization key (file or String)
            customKeyPassword = 'secret' // key password
        }

        // doc: Dry run
        localOnly = false // never connect to remote

        // doc: Uncommitted changes
        ignoreUncommittedChanges = true // should uncommitted changes force version bump

        // doc: Version / Tag with highest version
        useHighestVersion = false // Defaults as false, setting to true will find the highest visible version in the commit tree

        // doc: Version / Sanitization
        sanitizeVersion = true // should created version be sanitized, true by default

        tag { // doc: Version / Parsing
            prefix = 'tag-prefix' // prefix to be used, 'release' by default
            branchPrefix = [ // set different prefix per branch
                'legacy/.*' : 'legacy'
            ]

            versionSeparator = '-' // separator between prefix and version number, '-' by default
            serialize = { tag, version -> ... } // creates tag name from raw version
            deserialize = { tag, position, tagName -> ... } // reads raw version from tag
            initialVersion = { tag, position -> ... } // returns initial version if none found, 0.1.0 by default
        }

        nextVersion { // doc: Next version markers
            suffix = 'alpha' // tag suffix
            separator = '-' // separator between version and suffix
            serializer = { nextVersionConfig, version -> ... } // append suffix to version tag
            deserializer = { nextVersionConfig, position -> ... } // strip suffix off version tag
        }

        // doc: Version / Decorating
        versionCreator { version, position -> ... } // creates version visible for Gradle from raw version and current position in scm
        versionCreator 'versionWithBranch' // use one of predefined version creators
        branchVersionCreator = [ // use different creator per branch
            'feature/.*': 'default'
        ]

        // doc: Version / Incrementing
        versionIncrementer {context, config -> ...} // closure that increments a version from the raw version, current position in scm and config
        versionIncrementer 'incrementPatch' // use one of predefined version incrementing rules
        branchVersionIncrementer = [ // use different incrementer per branch
            'feature/.*': 'incrementMinor'
        ]

        // doc: Pre/post release hooks
        createReleaseCommit true // should create empty commit to annotate release in commit history, false by default
        releaseCommitMessage { version, position -> ... } // custom commit message if commits are created

        // doc: Pre-release checks
        checks {
            uncommittedChanges = false // permanently disable uncommitted changes check
            aheadOfRemote = false // permanently disable ahead of remote check
        }
    }
