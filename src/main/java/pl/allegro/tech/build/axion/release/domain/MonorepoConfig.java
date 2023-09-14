package pl.allegro.tech.build.axion.release.domain;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;

import java.util.LinkedList;
import java.util.List;

public abstract class MonorepoConfig extends BaseExtension {
    @Input
    public abstract ListProperty<String> getProjectDirs();

    @Internal
    public ListProperty<String> getExcludeDirs() { return getProjectDirs(); }

    public void exclude(String dir) {
        getProjectDirs().add(dir);
    }

    public void exclude(List<String> dirs) {
        getProjectDirs().addAll(dirs);
    }
}
