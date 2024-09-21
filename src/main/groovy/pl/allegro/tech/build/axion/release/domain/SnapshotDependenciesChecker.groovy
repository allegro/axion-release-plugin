package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencyConstraint

class SnapshotDependenciesChecker {

    Collection<String> snapshotVersions(Project project) {
        Collection<String> projectVersions = project.getRootProject().getAllprojects()
            .collect { toFullVersion(it) }
        Collection<String> allDependenciesVersions = []
        for (final Project proj in project.getRootProject().getAllprojects()) {
            for (Configuration config : proj.getConfigurations()) {
                Collection<String> versions = config.getAllDependencies()
                    .findAll { isSnapshot(it) }
                    .collect { toFullVersion(it) }
                +config.getAllDependencyConstraints()
                    .findAll { isSnapshot(it) }
                    .collect { toFullVersion(it) }
                allDependenciesVersions.addAll(versions)
            }
        }
        allDependenciesVersions = allDependenciesVersions.unique()
        allDependenciesVersions.removeAll(projectVersions)
        return allDependenciesVersions
    }

    boolean isSnapshot(Dependency dependency) {
        dependency.version?.endsWith("-SNAPSHOT")
    }

    boolean isSnapshot(DependencyConstraint dependency) {
        dependency.version?.endsWith("-SNAPSHOT")
    }

    String toFullVersion(it) {
        "${it.group}:${it.name}:${it.version}".toString()
    }
}
