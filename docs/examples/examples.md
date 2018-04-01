# Usage examples

## Basic Allegro setup

This is basic Allegro setup we use in most projects. Tag prefix is set to root project name
and each version has branch name appended (unless on master). This allows us on publishing snapshots
of branches that are ready for testing:

    scmVersion {
        tag {
            prefix = 'my-project-name'
        }
        versionCreator 'versionWithBranch'
    }


## Update README version

This replacement pattern will update any `version* x.x.x` occurrences in README.md and create release commit:

    scmVersion {
        tag {
            prefix = 'my-project-name'
        }
        versionCreator 'versionWithBranch'

        hooks {
            pre 'fileUpdate', [file: 'README.md', pattern: {v,p -> /(version.) $v/}, replacement: {v, p -> "\$1 $v"}]
            pre 'commit'
        }
    }
