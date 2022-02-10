# Using plugin with GitHub Actions

The plugin can be used with builds running via GitHub Actions.
However, the checkout action, (`actions/checkout@v2` at time of writing), does a shallow checkout without tags.
This is incompatible with the plugin, as it uses tags to track versions.

To use the plugin successfully the local git repository must have tag information.
Running `git fetch --tags --unshallow` _before_ running `./gradlew release` will ensure the plugin has all the info it needs to run.

The following example build file will use the quicker shallow fetch for pull-request builds against the `main` branch.
Whereas successful push builds will fetch the full repo history before running the release plugin:

```yaml
steps:
    - uses: actions/checkout@v2
    ...
    - name: Publish using Axion
      run: |
          # Fetch a full copy of the repo, as required by release plugin:
          git fetch --tags --unshallow
          # Run release:
          ./gradlew release
```
