package pl.allegro.tech.build.axion.release.domain;

import org.gradle.api.tasks.Internal;

import java.util.LinkedList;
import java.util.List;

public class MonorepoConfig {
    @Internal
    private List<String> projectDirs = new LinkedList<>();

    public List<String> getProjectDirs() {
        return projectDirs;
    }

    public void setProjectDirs(List<String> projectDirs) {
        this.projectDirs = projectDirs;
    }
}
