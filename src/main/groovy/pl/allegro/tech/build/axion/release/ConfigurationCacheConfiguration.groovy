package pl.allegro.tech.build.axion.release

class ConfigurationCacheConfiguration {
    boolean updateProjectVersionAfterRelease
    boolean configurationCacheEnabled
    public Closure<String> updateProjectVersion

    ConfigurationCacheConfiguration(boolean updateProjectVersionAfterRelease, Closure<String> updateProjectVersion) {
        this.updateProjectVersionAfterRelease = updateProjectVersionAfterRelease
        this.updateProjectVersion = updateProjectVersion
    }
}
