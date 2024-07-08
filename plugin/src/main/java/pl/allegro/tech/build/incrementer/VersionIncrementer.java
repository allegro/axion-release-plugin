package pl.allegro.tech.build.incrementer;

import com.github.zafarkhaja.semver.Version;

public interface VersionIncrementer {
    Version getNextVersion(Version currentVersion);
}
