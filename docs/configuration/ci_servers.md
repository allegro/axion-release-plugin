# Continuous integration servers

`axion-release-plugin` was designed to be CI friendly, which meant
adding some custom features. However CI build plans need to be
configured in certain way. Below are guides for tested CI servers.

CI servers is treated as *trusted* environment, thus there is no harm in
disabling checks that need to interact with git (like uncommitted files
check or ahead of remote check).

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
to matching local branch`.

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

## GitHub Actions

Your workflow needs to use `actions/checkout@v2` with configuration to [fetch tags](https://github.com/actions/checkout#fetch-all-history-for-all-tags-and-branches):

    steps:
      - uses: actions/checkout@v2
        with:
          fetch-depth: 0

When you have a lot of tags/commit you can speed up your build - plugin successfully works using local git shallow repository but you must run `git fetch --tags --unshallow` before running `./gradlew release` - that will ensure the plugin has all the info it needs to run. 

    steps:
        - uses: actions/checkout@v2
        - name: Publish using Axion
          run: |
              # Fetch a full copy of the repo, as required by release plugin:
              git fetch --tags --unshallow
              # Run release:
              ./gradlew release

In order to push tags into the repository release step must use GitHub actor and token:

      - name: Release
        id: release
        run: |
          ./gradlew release \
              -Prelease.customUsername=${{ github.actor }} \
              -Prelease.customPassword=${{ github.token }}


## GitLab CI

If you set up a [project token](https://docs.gitlab.com/ee/user/project/settings/project_access_tokens.html) you can easily add a non user dependent tag stage. Add the project token and token user bot name as CI-variables, accessible to the build script. 

Example:


    tagging:
      stage: tag
      image: ....
      script: 
       - git remote set-url origin ${CI_SERVER_URL}/${CI_PROJECT_NAMESPACE}/${CI_PROJECT_NAME}.git
       - ./gradlew release -Prelease.disableChecks -Prelease.pushTagsOnly -Prelease.overriddenBranchName=${CI_COMMIT_BRANCH} -Prelease.customUsername=${PROJECT_ACCESS_TOKEN_BOT_NAME} -Prelease.customPassword=${PROJECT_ACCESS_TOKEN}

NOTE: You need to set the git remote url first, as GitLab's default cloned project url will have added the non repo-write permision [gitlab-ci-token](https://docs.gitlab.com/ee/ci/jobs/ci_job_token.html) to the origin url.


Disabling checks is necessary because `axion-release` is not able to verify if current commit is ahead of remote. 
Setting pushTagsOnly ensures that git will not throw an error by attempting to push commits while not working on a branch.

Since Gitlab will do a detached head checkout, the branch name has to be overridden when `versionWithBranch` is used.
