Continuous integration servers
==============================

``axion-release-plugin`` was designed to be CI friendly, which meant adding some custom features. However CI build plans
need to be configured in certain way. Below are guides for tested CI servers.

CI servers is treated as *trusted* environment, thus there is no harm in disabling checks that need to interact
with git (like uncommitted files check or ahead of remote check).

Travis CI
---------

TBD

Jenkins
-------

Jenkins and ``axion-release`` cooperate nicely. However, because Jenkins will check out git repositories in a
'detached head' state, two flags should be set when running the release task::


    ./gradlew release -Prelease.disableChecks -Prelease.pushTagsOnly

Disabling checks is necessary because ``axion-release`` is not able to verify if current commit is ahead of remote.
Setting pushTagsOnly ensures that git will not throw an error by attempting to push commits while not working
on a branch.

Bamboo
------

Enable tags fetch
^^^^^^^^^^^^^^^^^

Bamboo fetches bare minimum of information from git. By default it won't even fetch tags. To change this:

* go to plan configuration
* open *Repositories* tab
* choose code repository
* open *Advanced options*
* disable *Use shallow clones* option

Attach remote on build
^^^^^^^^^^^^^^^^^^^^^^

Bamboo does not fetch remotes list. Fortunately ``axion-release`` can attach itself to remote using remote address passed
via Bamboo variables::


    ./gradlew release -s \
        -Prelease.attachRemote=${bamboo.repository.git.repositoryUrl} \
        -Prelease.disableChecks

We also need to disable checks, as there is no way to verify if current commit is ahead of remote.
