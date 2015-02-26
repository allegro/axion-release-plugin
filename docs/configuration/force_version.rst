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