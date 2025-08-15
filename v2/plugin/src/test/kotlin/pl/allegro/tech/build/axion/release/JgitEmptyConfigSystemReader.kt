package pl.allegro.tech.build.axion.release

import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.storage.file.FileBasedConfig
import org.eclipse.jgit.util.FS
import org.eclipse.jgit.util.SystemReader
import java.nio.file.Path

class JgitEmptyConfigSystemReader(private val projectDir: Path) : SystemReader() {
    override fun getenv(variable: String?): String? {
        return proxy.getenv(variable)
    }

    override fun getHostname(): String? {
        return proxy.getHostname()
    }

    override fun getProperty(key: String?): String? {
        return proxy.getProperty(key)
    }

    override fun getCurrentTime(): Long {
        return proxy.getCurrentTime()
    }

    override fun getTimezone(`when`: Long): Int {
        return proxy.getTimezone(`when`)
    }

    override fun openUserConfig(parent: Config?, fs: FS?): FileBasedConfig {
        return FileBasedConfig(parent, projectDir.toFile().resolve(".git/user-config"), fs)
    }

    override fun openJGitConfig(parent: Config?, fs: FS?): FileBasedConfig {
        return FileBasedConfig(parent, projectDir.toFile().resolve(".git/user-config"), fs)
    }

    override fun openSystemConfig(parent: Config?, fs: FS?): FileBasedConfig {
        return object : FileBasedConfig(parent, null, fs) {
            override fun load() {
            }

            override fun isOutdated(): Boolean {
                return false
            }
        }
    }

    companion object {
        private val proxy: SystemReader = getInstance()
    }
}
