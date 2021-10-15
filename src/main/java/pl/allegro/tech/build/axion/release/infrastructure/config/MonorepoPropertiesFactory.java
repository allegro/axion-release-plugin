package pl.allegro.tech.build.axion.release.infrastructure.config;

import org.gradle.api.Project;
import pl.allegro.tech.build.axion.release.domain.MonorepoConfig;
import pl.allegro.tech.build.axion.release.domain.properties.MonorepoProperties;

public class MonorepoPropertiesFactory {
    public static MonorepoProperties create(Project project, MonorepoConfig config, String currentBranch) {
        return new MonorepoProperties(config.getProjectDirs());
    }
}
