package pl.allegro.tech.build.axion.release.domain.scm

import org.gradle.api.Project

class ScmIdentityResolver {

    private static final String CUSTOM_KEY_PROPERTY = 'release.customKey'

    private static final String CUSTOM_KEY_FILE_PROPERTY = 'release.customKeyFile'

    private static final String CUSTOM_KEY_PASSWORD_PROPERTY = 'release.customKeyPassword'

    ScmIdentity resolve(Project project) {
        ScmIdentity identity
        if (project.hasProperty(CUSTOM_KEY_PROPERTY) || project.hasProperty(CUSTOM_KEY_FILE_PROPERTY)) {
            String key = resolveKey(project)
            identity = ScmIdentity.customIdentity(key, project.property(CUSTOM_KEY_PASSWORD_PROPERTY))
        } else {
            identity = ScmIdentity.defaultIdentity()
        }

        return identity
    }

    private String resolveKey(Project project) {
        String key
        if (project.hasProperty(CUSTOM_KEY_PROPERTY)) {
            key = project.property(CUSTOM_KEY_PROPERTY)
        } else {
            key = readKey(project.property(CUSTOM_KEY_FILE_PROPERTY))
        }
        return key
    }

    private String readKey(String keyFilePath) {
        return new File(keyFilePath).getText('UTF-8')
    }
}
