# Extending axion-release tasks

Since version 1.8.0, each task that is created by `axion-release` can be
used to create new task that extends it using standard Gradle
mechanisms.

For example to extend `OutputCurrentVersionTask` and add action that
will print additional message:

    plugins {
        // if you want axion-release tasks to be applied:
        id 'pl.allegro.tech.build.axion-release'
        // if you don't need them, add apply false:
        // id 'pl.allegro.tech.build.axion-release' apply false
    }

    import pl.allegro.tech.build.axion.release.domain.VersionConfig
    import pl.allegro.tech.build.axion.release.OutputCurrentVersionTask

    task customTask(type: OutputCurrentVersionTask) {
        versionConfig = project.objects.newInstance(VersionConfig,project.rootProject.layout.projectDirectory)
    }

    customTask.doFirst {
        println 'My custom message'
    }

Custom task can now be called:

    ./gradlew customTask
    My custom message
    Project version: 0.1.0-SNAPSHOT
