package pl.allegro.tech.build.axion.release.domain.properties;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MonorepoProperties {

    private final List<String> dirsToExclude;
    private final Set<String> dependenciesFolders;

    public MonorepoProperties() {
        this.dirsToExclude = Collections.emptyList();
        this.dependenciesFolders = Collections.emptySet();
    }

    public MonorepoProperties(List<String> dirsToExclude) {
        this.dirsToExclude = Collections.unmodifiableList(dirsToExclude);
        this.dependenciesFolders = Collections.emptySet();
    }

    public MonorepoProperties(List<String> dirsToExclude, Set<String> dependenciesFolders) {
        this.dirsToExclude = Collections.unmodifiableList(dirsToExclude);
        this.dependenciesFolders = dependenciesFolders;
    }

    public List<String> getDirsToExclude() {
        return dirsToExclude;
    }

    public Set<String> getDependenciesFolders() {
        return dependenciesFolders;
    }
}
