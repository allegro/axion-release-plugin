package pl.allegro.tech.build.axion.release

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.domain.ChecksConfig
import pl.allegro.tech.build.axion.release.domain.MonorepoConfig
import pl.allegro.tech.build.axion.release.domain.RepositoryConfig
import pl.allegro.tech.build.axion.release.domain.VersionConfig

final class Fixtures {
    static project() {
        return ProjectBuilder.builder().build()
    }

    static repositoryConfig(Project project = Fixtures.project()) {
        return project.objects.newInstance(RepositoryConfig, project.rootProject.layout.projectDirectory)
    }

    static versionConfig(Project project = Fixtures.project()) {
        return project.objects.newInstance(VersionConfig, project.rootProject.layout.projectDirectory)
    }

    static checksConfig(Project project = Fixtures.project()) {
        return project.objects.newInstance(ChecksConfig)
    }

    static monorepoConfig(Project project = Fixtures.project()) {
        return project.objects.newInstance(MonorepoConfig)
    }
}
