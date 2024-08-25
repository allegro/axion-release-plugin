# Configuration overview

All `axion-release-plugin` configuration options:

    scmVersion {

        repository {
            // doc: Repository
            type.set("git") // type of repository
            directory.set(project.rootProject.absolutePath) // repository location
            remote.set("origin") // remote name

            // doc: Authorization
            customKey.set("AAasaDDSSD...") // custom authorization key
            customKeyFile.set(project.file("myKey")) // custom authorization key (from file)
            customKeyPassword.set("secret") // key password
        }

        // doc: Dry run
        localOnly.set(false) // never connect to remote

        // doc: Uncommitted changes
        ignoreUncommittedChanges.set(true) // should uncommitted changes force version bump

        // doc: Version / Tag with highest version
        useHighestVersion.set(false) // Defaults as false, setting to true will find the highest visible version in the commit tree

        // doc: Version / Sanitization
        sanitizeVersion.set(true) // should created version be sanitized, true by default

        tag { // doc: Version / Parsing
            prefix.set("tag-prefix") // prefix to be used, "v" by default, empty String means no prefix
            branchPrefix.putAll( [ // set different prefix per branch
                "legacy/.*" : "legacy"
            ])

            versionSeparator.set("-") // separator between prefix and version number, "" by default, empty String means no separator
            serialize({ tag, version -> ... }) // creates tag name from raw version
            deserialize( { tag, position, tagName -> ... }) // reads raw version from tag
            initialVersion({ tag, position -> ... }) // returns initial version if none found, 0.1.0 by default
        }

        nextVersion { // doc: Next version markers
            suffix.set("alpha") // tag suffix
            separator.set("-") // separator between version and suffix
            serializer({ nextVersionConfig, version -> ... }) // append suffix to version tag
            deserializer({ nextVersionConfig, position -> ... }) // strip suffix off version tag
        }

        // doc: Version / Decorating
        versionCreator({ version, position -> ... }) // creates version visible for Gradle from raw version and current position in scm
        versionCreator("versionWithBranch") // use one of predefined version creators
        branchVersionCreator.putAll( [ // use different creator per branch
            "feature/.*": "default"
        ])

        // doc: Version / Snapshot
        snapshotCreator({ version, position -> ... }) // customize "snapshot" suffix for version not on tag

        // doc: Version / Incrementing
        versionIncrementer({context, config -> ...}) // closure that increments a version from the raw version, current position in scm and config
        versionIncrementer("incrementPatch") // use one of predefined version incrementing rules
        branchVersionIncrementer.putAll( [ // use different incrementer per branch
            "feature/.*": "incrementMinor"
        ])

        // doc: Pre/post release hooks
        createReleaseCommit.set(true) // should create empty commit to annotate release in commit history, false by default
        releaseCommitMessage({ version, position -> ... }) // custom commit message if commits are created

        // doc: Pre-release checks
        checks {
            uncommittedChanges.set(false) // permanently disable uncommitted changes check
            aheadOfRemote.set(false) // permanently disable ahead of remote check
        }

        // doc: Version / releaseOnlyOnReleaseBranches
        releaseOnlyOnReleaseBranches = false
        releaseBranchNames = ['master', 'main']
    }

All `axion-release-plugin` configuration flags:

- release.customKey
    - see [Authorization](authorization.md)
- release.customKeyFile
    - see [Authorization](authorization.md)
- release.customKeyPassword
    - see [Authorization](authorization.md)
- release.customUsername
    - see [Authorization](authorization.md) and [CI Servers](ci_servers.md)
- release.customPassword
    - see [Authorization](authorization.md) and [CI Servers](ci_servers.md)
- release.pushTagsOnly
    - see [Repository](repository.md) and [CI Servers](ci_servers.md)
- release.attachRemote
    - see [CI Servers](ci_servers.md)
- release.overriddenBranchName
    - possible values: any string like `develop`
    - usually the plugin detects the branch name automatically. With this flag, the result can be overridden.
    - see [CI Servers](ci_servers.md)
- release.overriddenIsClean
    - default: not set = determine the `isClean`-state
    - possible values: `true` or `false`
    - usually the plugin performs a check if the working directory is clean. With this flag, the result of the check can
      be overridden.
    - If you have a repository with a lot of files and do not use the isClean-feature, you may set this flag for a
      speed-up.
- release.disableSshAgent
    - default: false
    - do not use the ssh agent
- release.fetchTags
    - default: false
    - fetch tags from the remote repository
- release.releaseOnlyOnReleaseBranches
    - default: false
    - only perform a release on branches that match the `releaseBranchNames`
- release.releaseBranchNames
    - default: `['master', 'main']`
    - a list of branch names that are considered release branches
