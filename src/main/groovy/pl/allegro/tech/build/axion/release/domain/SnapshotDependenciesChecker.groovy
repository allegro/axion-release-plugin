package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

class SnapshotDependenciesChecker {

    Collection<String> snapshotVersions(Project project) {
        Collection<String> projectVersions = project.rootProject.allprojects.collect {toFullVersion(it)}
        Collection<String> allDependenciesVersions = project.allprojects.collect {
            it.configurations.collect { config ->
                config.allDependencies.findAll {isSnapshot(it)}.collect {toFullVersion(it)}

            }
        }.flatten().unique()
        allDependenciesVersions.removeAll(projectVersions)
        return allDependenciesVersions
    }

    boolean isSnapshot(Dependency dependency) {
        dependency.version?.endsWith("-SNAPSHOT")
    }

    String toFullVersion(it) {
        "${it.group}:${it.name}:${it.version}"
    }
}
