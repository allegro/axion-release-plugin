# Continuous integration servers

`axion-release-plugin` was designed to be CI friendly, which meant
adding some custom features. However, CI build plans need to be
configured in certain way. Below are guides for tested CI servers.

CI servers is treated as *trusted* environment, thus there is no harm in
disabling checks that need to interact with git (like uncommitted files
check or ahead of remote check).

## Shallow clones

Many CI servers use shallow clone to optimize repository fetching (for example GitHub actions). However, if only
1 commit from the top of the branch is fetched, `axion-release` doesn't see the latest tag and is unable to determine
the correct version.

Because of this issue, `axion-release` will automatically unshallow the repository if executed on CI server.
To disable it, use:

    scmVersion {
        unshallowRepoOnCI.set(false)
    }

This behavior is experimental and has been tested on the following CI servers:

- GitHub Actions

## GitHub Actions

`axion-release` has dedicated support for GitHub Actions and you don't need any custom configs to make it working.

### Compatibility with actions/checkout

`axion-release` is fully compatible with all versions of `actions/checkout`, including v6 and later. 

Starting with `actions/checkout@v6`, credentials are stored in a separate config file under `$RUNNER_TEMP` and referenced via `includeIf.gitdir` directives. While JGit doesn't natively support these directives, `axion-release` automatically detects and loads credentials from these files, ensuring seamless authentication for push operations.

### GitHub outputs

To make it easier for you to chain jobs in a workflow, `axion-release` will provide some information as GitHub outputs.

| name                | description                                 |
|---------------------|---------------------------------------------|
| `released-version`  | Provided after executing the `release` task |
| `published-version` | Provided after executing the `publish` task |

#### Multi-version builds

If all your Gradle modules use the same version, the output will be a single value, such as:
```
1.0.0
```

However, if each module has its own version, the output will be in JSON format, for example:
```json
{"root-project":"1.0.0","sub-project-1":"2.0.0","sub-project-2":"3.0.0"}
```
where `root-project`, `sub-project-1` and `sub-project-2` are project names from Gradle.

#### Example

```yaml
jobs:
  build:
    steps:
      - id: release
        run: ./gradlew release

      # for single-version builds
      - run: |
          echo ${{ steps.release.outputs.released-version }}

      # for multi-version builds
      - run: |
          echo ${{ fromJson(steps.release.outputs.released-version).root-project }}
          echo ${{ fromJson(steps.release.outputs.released-version).sub-project-1 }}
          echo ${{ fromJson(steps.release.outputs.released-version).sub-project-2 }}
```

## Jenkins

Jenkins and `axion-release` cooperate nicely. However, because Jenkins
will check out git repositories in a `detached head` state, two flags
should be set when running the release task:

    ./gradlew release -Prelease.disableChecks -Prelease.pushTagsOnly

Disabling checks is necessary because `axion-release` is not able to
verify if current commit is ahead of remote. Setting pushTagsOnly
ensures that git will not throw an error by attempting to push commits
while not working on a branch.

To use the [versionWithBranch](version.md#versionwithbranch-default) version creator from Jenkins,
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

Bamboo fetches bare minimum of information from git. By default, it
won't even fetch tags. To change this:

- go to plan configuration
- open *Repositories* tab
- choose code repository
- open *Advanced options*
- disable *Use shallow clones* option

### Attach remote on build

Bamboo does not fetch remotes list. Fortunately `axion-release` can
attach itself to the remote using remote address passed via Bamboo
variables:

    ./gradlew release -s \
        -Prelease.attachRemote=${bamboo.repository.git.repositoryUrl} \
        -Prelease.disableChecks

We also need to disable checks, as there is no way to verify if current
commit is ahead of remote.

## Bitbucket Pipelines

Bitbucket Pipelines allow to commit to repository without any configuration.
However, it requires git client to use local proxy. It can be configured by passing following parameters:

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

## GitLab CI

If you set up a [project token](https://docs.gitlab.com/ee/user/project/settings/project_access_tokens.html) you can
easily add a non-user dependent tag stage. Add the project token and token user bot name as CI-variables, accessible to
the build script.

Example:

    tagging:
      stage: tag
      image: ....
      script:
       - git remote set-url origin ${CI_SERVER_URL}/${CI_PROJECT_NAMESPACE}/${CI_PROJECT_NAME}.git
       - ./gradlew release -Prelease.disableChecks -Prelease.pushTagsOnly -Prelease.overriddenBranchName=${CI_COMMIT_REF_SLUG} -Prelease.customUsername=${PROJECT_ACCESS_TOKEN_BOT_NAME} -Prelease.customPassword=${PROJECT_ACCESS_TOKEN}

NOTE: You need to set the git remote url first, as GitLab's default cloned project url will have added the non
repo-write permission [gitlab-ci-token](https://docs.gitlab.com/ee/ci/jobs/ci_job_token.html) to the origin url.

Disabling checks is necessary because `axion-release` is not able to verify if current commit is ahead of remote.
Setting pushTagsOnly ensures that git will not throw an error by attempting to push commits while not working on a
branch.

Since Gitlab will do a detached head checkout, the branch name has to be overridden when `versionWithBranch` is used.
