# Release hooks

`axion-release-plugin` supports registering custom actions to be hooked
either before (*pre*) or after (*post*) the release:

    scmVersion {
        hooks {
            pre {context -> ...}
            post {context -> ...}
        }
    }

Implementing action for common tasks would be a waste, this is why
`axion-release` comes bundled with two predefined actions. If you come
up with some useful implementation, don't hesitate to create pull
request!

## fileUpdate

This action can update given file (or files) by evaluating regex pattern
and replacing all matches with given replacement. Most common scenario
is to update version in README:

    scmVersion {
        hooks {
            pre 'fileUpdate', [file: 'README.md', pattern: {v, c -> /version: $v/}, replacement: {v, c -> "version: $v"}, encoding: 'utf-8']
        }
    }

Syntax of action is simple: first comes name, then map of arguments.
Supported arguments:

- file - path to file in form of string or `File` instance to update
- files - array of files, takes precedence over single file definition
    if not empty
- pattern - closure that should return String pattern, arguments are
    **previous version** and context (described below)
- replacement - closure that should return replacement, arguments are
    **current version** and context
- encoding - the name of encoding to use during processing the specified files.
    By default, it uses default JVM encoding (described below)

All patterns have multiline flag switched on by default to match against
content of whole file.

All files are processed using JVM encoding that by default is equal to the system's encoding,
so this operation should be considered as OS-dependent.
To avoid consequences of such behaviour,
it's recommended to explicitly declare project's encoding through
e.g. `-Dfile.encoding=<encoding>` flag in `gradle.properties` file.
For individual cases, the `fileUpdate.encoding` can be used.

File update operation adds all changed files to set of files to commit
in context, which are later processed by **commit** hook.

## commit

Creates commit. Only files (patterns) that were added to context via
`HookContext#addCommitPattern` get committed. Remember, that
**fileUpdate** does it for you!:

    scmVersion {
        hooks {
            pre 'fileUpdate', [...]
            pre 'commit'
        }
    }

Default commit message is `Release version: $version`.
It can be customized by passing closure that accepts current version and SCM position as arguments:

    scmVersion {
       hooks {
            pre 'commit', {v, p -\> "This is my great new commit message for version $v"}
        }
    }

## push

Pushes all changes to the remote::

    scmVersion {
        hooks {
            post 'push'
        }
    }

There is no additional magic in this action. Use with care, only when you have some commits that need to be pushed for
example as a post action. Remember, that push is always done during the release.

## Custom action

Of course nothing can stop you from implementing own action. It can be
any closure that accepts `HookContext` object:

    scmVersion {
        hooks {
            pre {context -> /* do something important */}
        }
    }

`HookContext` object fields and methods:

-   *logger* - instance of Gradle project logger
-   *position* - [ScmPosition](https://github.com/allegro/axion-release-plugin/blob/master/src/main/java/pl/allegro/tech/build/axion/release/domain/scm/ScmPosition.java)
-   *previousVersion* - version before release
-   *currentVersion* - released version
-   *readVersion()* - force reevaluation of version, returns fresh
    version as if called `./gradlew currentVersion` command
-   *commit(List patterns, String message)* - force commit of given
    patterns with message
-   *addCommitPattern(String pattern)* - add pattern to commit, you can
    commit everything at once by adding commit hook to the chain
