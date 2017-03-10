Use Highest Version
===================

By default ``axion-release`` uses the first tag it can find in the git tree. Setting this to true will cause it
to find the highest version visible in the git tree's history.

In order to activate this feature::

    scmVersion {
        useHighestVersion = true
    }
    
With a tree similar to this::

    Tag: 1.0.0
    Tag: 1.5.0
    Tag: 1.2.0

This changes behavior from::

    # ./gradlew currentVersion
    1.2.0
    
to::
    
    # ./gradlew currentVersion 
    1.5.0
    
You can also active this option using command line::

    # ./gradlew currentVersion -Prelease.useHighestVersion
    1.5.0