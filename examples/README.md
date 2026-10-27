# Examples

Both examples apply Tagline in settings. The plugin assigns the resolved version to every project before its build
script runs. Each project can access `ProjectTaglineExtension`; task inputs capture its immutable version information.

From the repository root, using an installed JDK 21 or newer supported by Gradle:

```sh
./gradlew -p examples/single-module currentVersion versionDetails generatePomFileForLibraryPublication
./gradlew -p examples/multi-module currentVersion versionDetails :library:versionDetails :library:generatePomFileForLibraryPublication
```

These commands only read Git and generate local reports/POMs. `versionDetails` writes `build/version.properties`;
`generatePomFileForLibraryPublication` writes `build/publications/library/pom-default.xml`.
Local builds use the enclosing repository; no remote or token is required. On GitHub Actions, Tagline instead uses the
workflow metadata/API, including with a shallow checkout. See [the version contract](../version-contract.md) for event
and credential requirements. The root wrapper can run both examples; the single-module example also has its own wrapper.

The functional tests copy these build scripts into isolated directories and run real Gradle builds. They check versions
in both task inputs and generated publication metadata, including a GitHub build with no `.git` directory.
