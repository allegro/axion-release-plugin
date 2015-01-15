package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project

class RepositoryConfigFactory {

    private static final String CUSTOM_KEY_PROPERTY = 'release.customKey'

    private static final String CUSTOM_KEY_FILE_PROPERTY = 'release.customKeyFile'

    private static final String CUSTOM_KEY_PASSWORD_PROPERTY = 'release.customKeyPassword'


    static RepositoryConfig create(Project project) {
        RepositoryConfig config = new RepositoryConfig()
        config.directory = project.rootDir

        if (project.hasProperty(CUSTOM_KEY_PASSWORD_PROPERTY)) {
            config.customKeyPassword = project.property(CUSTOM_KEY_PASSWORD_PROPERTY)
        }

        if (project.hasProperty(CUSTOM_KEY_PROPERTY)) {
            config.customKey = project.property(CUSTOM_KEY_PROPERTY)
        } else if (project.hasProperty(CUSTOM_KEY_FILE_PROPERTY)) {
            config.customKey = readKey(project.property(CUSTOM_KEY_FILE_PROPERTY))
        }
        return config
    }

    private static String readKey(String keyFilePath) {
        return new File(keyFilePath).getText('UTF-8')
    }
}
