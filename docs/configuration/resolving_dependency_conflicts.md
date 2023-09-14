# Resolving dependency conflicts

`axion-release-plugin` uses [JGit](https://www.eclipse.org/jgit/) and [JSch](http://www.jcraft.com/jsch/)
 under the hood. Other Gradle plugins can have version conflicts with those dependencies.

In order to resolve such conflicts specify dependency explicitly. Eg. for JGit:

    buildscript {
        dependencies {
            classpath("pl.allegro.tech.build:axion-release-plugin:<version>") {
                exclude group: "org.eclipse.jgit"
            }
            classpath("org.eclipse.jgit:org.eclipse.jgit:5.12.0.202106070339-r")
        }
    }
