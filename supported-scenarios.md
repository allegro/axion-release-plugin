# Supported scenarios

The authoritative rules and limitations are in [version-contract.md](version-contract.md).

| Scenario | Implemented outcome | Evidence |
|---|---|---|
| GitHub tag push without checkout/token | Exact tag version; no API call | Gradle integration test using the single-module example and generated Maven POM |
| GitHub published release without checkout/token | Payload tag version | Same integration test, published-release event |
| GitHub branch push on an already tagged commit | Exact version wins over a higher repository tag | Paginated HTTP fixture, multi-project Gradle build, generated POM |
| Untagged GitHub feature branch | Highest version increment, sanitized branch suffix, SNAPSHOT | Same multi-project integration test |
| GitHub manual build on default branch | Incremented version without branch or SNAPSHOT suffix | Same multi-project integration test |
| Shared version in root/subproject | Project API and publication version agree | Multi-module example executed by Gradle TestKit |
| Configuration-cache reuse and invalidation | Unchanged lookup reused; changed built revision recalculated | GitHub and local integration tests |
| Local annotated tag, then new commit | Exact tag version, then incremented version without SNAPSHOT on the default branch | Temporary real repositories, both Git CLI and JGit |
| Local nearest, highest reachable, and highest global | Distinct historical versions; an exact tag wins under all strategies | One Gradle integration test with older/newer tags and a separate branch, both local backends |
| Unknown CI, missing GitHub credentials, unsupported event/strategy | Falls back to Git CLI, then JGit if Git is unavailable | Gradle integration test with a real local repository and populated/empty PATH |
| Provider HTTP failure or offline build | Resolves local tags; offline mode skips the API | Same integration test with an HTTP 503 fixture |
| Initial versions, major/minor/patch, prereleases, custom formatting | Contract-defined results | Pure resolver tests without mocks |
| Provider environment conversion | Shared domain metadata, native endpoints, ordered token aliases, redacted credentials | Focused conversion tests with GitHub/GitLab inputs |
| Explicit Git/JGit with GitHub or GitLab on a detached checkout | Retains CI branch/revision, reads local tags, skips CI API; publication and configuration cache work | Gradle integration test with a real repository and HTTP fixture |
| Unavailable CI metadata or inactive providers | Uses local context | Focused environment conversion test |
| Failed provider lookup and failed local fallback | Reports the local failure and preserves the provider failure; fallback reasons appear with `--info` | Focused fallback tests and Gradle integration coverage |
| Malformed GitHub payload or failed tag API transport | Provider boundaries normalize failures before local fallback | Focused conversion and HTTP fixture tests |

PR/MR events, Jenkins, complete GitLab resolution, filesystem Git parsing, independent module versions, and release
creation remain deferred. GitHub API lookup uses a token; local fallback handles unavailable provider resolution.
There is no `gh` CLI fallback.
