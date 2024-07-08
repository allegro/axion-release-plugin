package pl.allegro.tech.build.incrementer;

import com.github.zafarkhaja.semver.Version;

/**
 * Simple version incrementer that increments patch version.
 */
public class SimpleVersionIncrementer implements VersionIncrementer {
    @Override
    public Version getNextVersion(Version currentVersion) {
        return currentVersion.nextPatchVersion();
    }
}
