# Continuous integration servers

`axion-release-plugin` was designed to be CI friendly, which meant
adding some custom features. However CI build plans need to be
configured in certain way. Below are guides for tested CI servers.

CI servers is treated as *trusted* environment, thus there is no harm in
disabling checks that need to interact with git (like uncommitted files
check or ahead of remote check).

## Travis CI

TBD

## Jenkins

Jenkins and `axion-release` cooperate nicely. However, because Jenkins
will check out git repositories in a `detached head` state, two flags
should be set when running the release task:

    ./gradlew release -Prelease.disableChecks -Prelease.pushTagsOnly

Disabling checks is necessary because `axion-release` is not able to
verify if current commit is ahead of remote. Setting pushTagsOnly
ensures that git will not throw an error by attempting to push commits
while not working on a branch.

To use the [versionWithBranch](version.md#versionwithbranch) version creator from Jenkins,
you need to override the default behavior of the Jenkins git plugin to
avoid the `detached-head` state. In the Git section of the job
configuration page, add the `Additional Behaviour` called `Check out
to specific local branch` and enter your branch name.

Jenkins pipeline now defaults to clone with a narrow refspec, and
without tags (as of git plugin 3.4.0). That saves network bandwidth,
time, and disc space. If you need tags in your Jenkins workspace, add
the `Additional Behaviour` called `Advanced clone behaviors`. Adding
that behaviour will enable fetching of tags.

## Bamboo

### Enable tags fetch

Bamboo fetches bare minimum of information from git. By default it
won't even fetch tags. To change this:

-   go to plan configuration
-   open *Repositories* tab
-   choose code repository
-   open *Advanced options*
-   disable *Use shallow clones* option

### Attach remote on build

Bamboo does not fetch remotes list. Fortunately `axion-release` can
attach itself to remote using remote address passed via Bamboo
variables:

    ./gradlew release -s \
        -Prelease.attachRemote=${bamboo.repository.git.repositoryUrl} \
        -Prelease.disableChecks

We also need to disable checks, as there is no way to verify if current
commit is ahead of remote.

## Bitbucket Pipelines

Bitbucket Pipelines allow to commit to repository without any configuration.
However it requires git client to use local proxy. It can be configured by passing following parameters:

    ./gradlew release -Dhttp.proxyHost=localhost -Dhttp.proxyPort=29418

## Azure DevOps Pipelines

Azure Pipelines will check out git repositories in a `detached head` state.
That is why two flags should be set when running the release task:

    ./gradlew release -Prelease.disableChecks -Prelease.pushTagsOnly

Disabling checks is necessary because `axion-release` is not able to
verify if current commit is ahead of remote. Setting pushTagsOnly
ensures that git will not throw an error by attempting to push commits
while not working on a branch.

To use features related to branches (like [versionWithBranch](version.md#versionwithbranch),
[branchVersionIncrementer](version.md#incrementing) or [branchVersionCreator](version.md#decorating))
you need to override branch name with `overriddenBranchName` flag and set it to
`Build.SourceBranch` Azure Pipelines predefined variable:

    ./gradlew release \
        -Prelease.disableChecks \
        -Prelease.pushTagsOnly \
        -Prelease.overriddenBranchName=$(Build.SourceBranch)
