package pl.allegro.tech.build.axion.release.domain;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencyConstraint;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SnapshotDependenciesChecker {

    public Collection<String> snapshotVersions(Project project) {
        Set<String> projectVersions = project.getRootProject().getAllprojects().stream()
            .map(this::toFullVersion)
            .collect(Collectors.toSet());

        Set<Configuration> configurations = project.getRootProject().getAllprojects().stream()
            .flatMap(p -> p.getConfigurations().stream())
            .collect(Collectors.toSet());

        Set<String> allDependenciesVersions = new HashSet<>();
        for (Configuration config : configurations) {
            Set<String> versions = config.getAllDependencies().stream()
                .filter(this::isSnapshot)
                .map(this::toFullVersion)
                .collect(Collectors.toSet());

            Set<String> constraintVersions = config.getAllDependencyConstraints().stream()
                .filter(this::isSnapshot)
                .map(this::toFullVersion)
                .collect(Collectors.toSet());

            allDependenciesVersions.addAll(versions);
            allDependenciesVersions.addAll(constraintVersions);
        }
        allDependenciesVersions.removeAll(projectVersions);
        return allDependenciesVersions;
    }

    private boolean isSnapshot(Dependency dependency) {
        return dependency.getVersion() != null && dependency.getVersion().endsWith("-SNAPSHOT");
    }

    private boolean isSnapshot(DependencyConstraint dependency) {
        return dependency.getVersion() != null && dependency.getVersion().endsWith("-SNAPSHOT");
    }

    private String toFullVersion(Object it) {
        if (it instanceof Dependency) {
            Dependency dependency = (Dependency) it;
            return String.format("%s:%s:%s", dependency.getGroup(), dependency.getName(), dependency.getVersion());
        } else if (it instanceof DependencyConstraint) {
            DependencyConstraint constraint = (DependencyConstraint) it;
            return String.format("%s:%s:%s", constraint.getGroup(), constraint.getName(), constraint.getVersion());
        }
        return "";
    }
}
