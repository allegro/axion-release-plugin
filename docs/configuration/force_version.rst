Overriding version
==================

If you are not happy with version provided by ``axion-release`` you can force own version using ``release.forceVersion``
flag::

    ./gradlew release -Prelease.forceVersion=3.0.0

Forced version is treated as version read from repository, so it still will go through version
:ref:`version-decorating` process::

    # ./gradlew currentVersion -Prelease.forceVersion=3.0.0
    3.0.0-SNAPSHOT
    
    # git checkout my-branch
    
    # ./gradlew currentVersion -Prelease.forceVersion=3.0.0
    3.0.0-my-branch-SNAPSHOT

Empty ``release.forceVersion`` is ignored.


Force snapshot version
----------------------

If you want to override the default 'stableness' check logic, you can ask axion to generate snapshot version (as if you
would have some committed changes) using ``release.forceSnapshot`` flag. It's especially useful with integration with CI
servers when you want to ensure that CI build plans always to produce and publish snapshot artifacts (contrary to
release plans):

    ./gradlew build publish -Prelease.forceSnapshot

