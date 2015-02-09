Publishing
==========

Publishing release version is simple with ``axion-release-plugin``. Since release does not increase version
unless you commit something, you can publish release version any time by calling gradle once again::


    ./gradlew release
    ./gradlew publish


Why not make it work in single Gradle run? **maven-publish** plugin reads **project.version** in 
configuration phase. Any change made by tasks running prior to publishing won't be recognized.
