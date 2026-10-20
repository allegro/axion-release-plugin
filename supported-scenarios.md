# Environment and event selection

`backend = AUTO` detects the execution environment first. GitHub Actions and GitLab CI remain the selected environments
for every event. If neither is detected, the plugin uses Git CLI when it is on `PATH`, or JGit otherwise.

| Environment signal                     | Selected environment |
|----------------------------------------|----------------------|
| `GITHUB_ACTIONS` is set                | GitHub               |
| `GITLAB_CI` is set                     | GitLab               |
| Neither is set, Git CLI is available   | Local Git CLI        |
| Neither is set, Git CLI is unavailable | Local JGit           |

The selected provider classifies its event separately:

| Provider | Event signal                              | Event strategy         |
|----------|-------------------------------------------|------------------------|
| GitHub   | Published release payload                 | Use the release's tag  |
| GitHub   | Tag push                                  | Use `GITHUB_REF_NAME`  |
| GitHub   | Branch push                               | Look up a version tag  |
| GitHub   | `workflow_dispatch`                       | Look up a version tag  |
| GitLab   | `CI_COMMIT_TAG` is nonempty               | Use the pipeline's tag |
| GitLab   | Branch push                               | Look up a version tag  |
| GitLab   | Manual pipeline (`web`, `api`, `trigger`) | Look up a version tag  |
| Local    | Any run                                   | Look up a version tag  |

For highest-version lookup, GitHub uses its repository tags API when `GH_TOKEN` or `GITHUB_TOKEN` and
`GITHUB_REPOSITORY` are present. Without a token, it uses an available authenticated `gh` CLI. GitLab uses its
repository tags API when `CI_PROJECT_ID` and `CI_JOB_TOKEN`, `GITLAB_TOKEN`, or `GLAB_TOKEN` are present. Both APIs
paginate and compare parsed versions rather than assuming API order is version order. When no provider lookup is
available, lookup uses local Git CLI or JGit. Nearest reachable tag always uses local repository history.

A workflow must expose its GitHub token to the Gradle process and grant `contents: read` for private repository tags.
`backend = GITHUB` and `backend = GITLAB` require the matching provider marker. `backend = GIT` and `backend = JGIT`
explicitly use the local implementation.
