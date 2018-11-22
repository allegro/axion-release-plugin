# Changelog

* **1.9.4** (22.11.2018)
    * fixes next version behavior when used with force snapshot - thanks to [Theer108](https://github.com/Theer108) and [trohr](https://github.com/trohr) for contribution
* **1.9.3** (26.08.2018)
    * `nextVersion -Prelease.incrementer=...` can create next version using selected incrementer - thanks to [jplucinski](https://github.com/jplucinski) for contribution
    * adds user-friendly message when no tracking branch present
* **1.9.2** (15.06.2018)
    * further clarification of behavior when multiple tags on single commit - see `VersionSorter` class and tests
* **1.9.1** (20.05.2018)
    * fixes behavior when two tags on the same commit - thanks to [lwasylkowski](https://github.com/lwasylkowski) for contribution
* **1.9.0** (23.02.2018)
    * removes GrGit dependency
* **1.8.3** (30.01.2018)
    * fixes issue with obtaining SSH credentials - thanks to [Cliffred van Velzen](https://github.com/cliffred) for contribution
    * issue with ordering of tags when using alpha versions - thanks to [Cliffred van Velzen](https://github.com/cliffred) for contribution
* **1.8.2** (29.12.2017)
    * prints errors from remote when remote rejects push (they were silently ignored before this change) - thanks to [mareck](https://github.com/mareckmareck) for contribution
    * `-Prelease.forceVersion` no longer forces -SNAPSHOT when forced version is equal to current version
* **1.8.1** (07.10.2017)
    * fixes in Gradle Portal publishing code, verison 1.8.0 was not published correctly
* **1.8.0** (07.10.2017)
    * allows on extending axion-release tasks - thanks to [Maria Camenzuli](https://github.com/maria-camenzuli) for contribution
    * moves travis builds to JDK 8 only - from this time on JDK 7 compatibility is no longer tested
* **1.7.2** (29.08.2017)
    * better performance when scanning for tags - lazy scan instead of reading all commits at once
    * adds graceful failure on version parsing problems
    * chooses normal version over nextVersion when both on same commit
* **1.7.1** (05.07.2017)
    * fixes error when working on project without Git repository
* **1.7.0** (16.07.2017)
    * compatibility with Gradle 4.0
* **1.6.0** (13.04.2017)
    * added option to find highest version from all tags, not only current branch - thanks to [ProTrent](https://github.com/ProTrent) for contribution
* **1.5.0** (06.02.2017)
    * added ordering of tags by version when multiple found on single commit
* **1.4.1** (11.10.2016)
    * fixing critical bug in printing Git changes, which could block `verifyRelease` task
* **1.4.0** (17.08.2016)
    * compatibility with Gradle 3.0
* **1.3.5** (23.06.2016)
    * added `tagSelector` option to choose from multiple tags on single commit - thanks to [levsa](https://github.com/levsa) for contribution
    * added snapshot dependencies check - thanks to [vbuell](https://github.com/vbuell) for contribution
* **1.3.4** (30.12.2015)
    * added option to specify tag prefix per branch
    * added option to specify version incrementer per branch
    * added option to use predefined version incrementers and creators in per-branch settings
    * (internal) separated all version management logic from Gradle
* **1.3.3** (19.11.2015)
    * added `release.version` argument that should be used instead of `release.forceVersion` and `release.nextVersion`
* **1.3.2** (24.08.2015)
    * added possibility to read uncached, current version in hooks
* **1.3.1** (16.08.2015)
    * added option to treat uncommitted changes as repository change
    * added `push` hook action
* **1.3.0** (11.07.2015)
    * support for custom version incrementation rules - thanks to [vbuell](https://github.com/vbuell) for contribution
    * ability to push only tags to remote repo - thanks to [erichsend](https://github.com/erichsend) for contribution
* **1.2.4** (03.05.2015)
    * support for pre-release versions (i.e. 2.0.0-rc2) - thanks to [vbuell](https://github.com/vbuell) for contribution
* **1.2.3** (29.03.2015)
    * support for Basic Auth username and password (GitHub tokens!)
* **1.2.2** (21.03.2015)
    * use newer GrGit (and JGit) version with some bugfixes
    * start using Coveralls
* **1.2.1** (08.03.2015)
    * create separate tasks for tag creation and pushing
    * possibility to use different configuration across multiple modules
* **1.2.0** (09.02.2015)
    * hooks - add custom actions before/after release
    * new documentation in RTD
* **1.1.0** (02.02.2015)
    * next version markers - ability to declare working on next major version
* **1.0.1** (15.01.2015)
    * support for setting custom key file/password in runtime
    * printing uncommitted changes on check failure
* **1.0.0** (12.12.2014)
    * support for different means of authorization


* **0.9.9** (01.12.2014)
    * added option to choose Git repository root
* **0.9.8** (24.11.2014)
    * publication in gradle plugin portal
* **0.9.5** (24.11.2014)
    * added option to create empty commit to mark release (#18)
    * compatibility with newer Semver 0.8.0 (#19)
* **0.9.4** (07.11.2014)
    * added option to run whole process locally, without interaction with remote (#8, #9)
    * default tag name serializers/deserializers are smarter (#13)
    * fixed bug #10, pushes always went to origin instead of defined remote
* **0.9.3** (16.10.2014)
    * added predefined version creators
* **0.9.2** (06.10.2014)
    * optional version sanitization to protect against any strange chars in artifact names
    * private key password is not obligatory anymore
* **0.9.1** (25.09.2014)
    * fixed bug with bamboo CI >= 5.6 (#3)
* **0.9.0** (23.09.2014)
    * refactored API to be more verbose
    * refactored all configuration options
    * added per-branch version settings
* **0.3.0** (24.08.2014)
    * removed tags fetching option as it was inefficient in CI anyway
* **0.2.8** (22.08.2014)
    * added option to pass custom SSH key
    * added option to fetch tags before resolving version
* **0.2.6** (19.08.2014)
    * possibility to attach remote repository, useful in Bamboo CI builds
* **0.2.5** (18.08.2014)
    * documented, final version of dry-run
    * more verbose logging
    * options to disable checks (verification) before release
* **0.2.4** (?)
    * added dry-run capability
