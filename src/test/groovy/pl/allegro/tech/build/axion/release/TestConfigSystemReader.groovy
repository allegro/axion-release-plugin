package pl.allegro.tech.build.axion.release

import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.storage.file.FileBasedConfig
import org.eclipse.jgit.util.FS
import org.eclipse.jgit.util.SystemReader

class TestConfigSystemReader extends SystemReader {
    private static final SystemReader proxy = getInstance()
    private final File userGitConfig

    TestConfigSystemReader(File userGitConfig) {
        super()
        this.userGitConfig = userGitConfig
    }

    @Override
    String getenv(String variable) {
        return proxy.getenv(variable)
    }

    @Override
    String getHostname() {
        return proxy.getHostname()
    }

    @Override
    String getProperty(String key) {
        return proxy.getProperty(key)
    }

    @Override
    long getCurrentTime() {
        return proxy.getCurrentTime()
    }

    @Override
    int getTimezone(long when) {
        return proxy.getTimezone(when)
    }

    @Override
    FileBasedConfig openUserConfig(Config parent, FS fs) {
        return new FileBasedConfig(parent, userGitConfig, fs)
    }

    @Override
    FileBasedConfig openJGitConfig(Config parent, FS fs) {
        return proxy.openJGitConfig(parent, fs)
    }

    @Override
    FileBasedConfig openSystemConfig(Config parent, FS fs) {
        return new FileBasedConfig(parent, null, fs) {
            @Override
            void load() {
            }

            @Override
            boolean isOutdated() {
                return false
            }
        }
    }
}
