axion-release-plugin changelog
====

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
