package pl.allegro.tech.build.axion.release.infrastructure.git;

import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.SystemReader;

public class SystemReaderWithoutSystemConfig extends SystemReader {
    private static final SystemReader DefaultSystemReader = SystemReader.getInstance();
    private final boolean ignoreUserSettings;

    public SystemReaderWithoutSystemConfig(boolean ignoreUserSettings) {
        super();
        this.ignoreUserSettings = ignoreUserSettings;
    }

    @Override
    public String getenv(String variable) {
        return DefaultSystemReader.getenv(variable);
    }

    @Override
    public String getHostname() {
        return DefaultSystemReader.getHostname();
    }

    @Override
    public String getProperty(String key) {
        return DefaultSystemReader.getProperty(key);
    }

    @Override
    public long getCurrentTime() {
        return DefaultSystemReader.getCurrentTime();
    }

    @Override
    public int getTimezone(long when) {
        return DefaultSystemReader.getTimezone(when);
    }

    @Override
    public FileBasedConfig openUserConfig(Config parent, FS fs) {
        if (ignoreUserSettings) return new EmptyFileBasedConfig(parent, fs);
        return DefaultSystemReader.openUserConfig(parent, fs);
    }

    @Override
    public FileBasedConfig openJGitConfig(Config parent, FS fs) {
        return DefaultSystemReader.openJGitConfig(parent, fs);
    }

    // Return an empty system configuration to prevent JGit from accessing it
    // This resolves issues with Gradle being unable to save configuration cache
    // Based on https://stackoverflow.com/a/59110721
    @Override
    public FileBasedConfig openSystemConfig(Config parent, FS fs) {
        return new EmptyFileBasedConfig(parent, fs);
    }

    private static class EmptyFileBasedConfig extends FileBasedConfig {
        public EmptyFileBasedConfig(Config parent, FS fs) {
            super(parent, null, fs);
        }

        @Override
        public void load() {
        }

        @Override
        public boolean isOutdated() {
            return false;
        }
    }
}
