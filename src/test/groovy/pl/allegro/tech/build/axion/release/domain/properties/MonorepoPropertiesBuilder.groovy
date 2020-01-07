package pl.allegro.tech.build.axion.release.domain.properties

class MonorepoPropertiesBuilder {

    private List<String> dirsToExclude = []

    private MonorepoPropertiesBuilder() {
    }

    static MonorepoPropertiesBuilder monorepoProperties() {
        return new MonorepoPropertiesBuilder()
    }

    MonorepoProperties build() {
        return new MonorepoProperties(dirsToExclude)
    }

    MonorepoPropertiesBuilder excludeDirs(List<String> dirsToExclude) {
        this.dirsToExclude = dirsToExclude
        return this
    }
}
