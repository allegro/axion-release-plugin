package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

import javax.inject.Inject
import java.util.regex.Pattern

class VersionConfig {

    private final Project project

    String repository = 'git'

    File repositoryDir

    String remote = 'origin'

    boolean localOnly = false

    TagNameSerializationRules tag = new TagNameSerializationRules()

    Closure versionCreator = PredefinedVersionCreator.DEFAULT.versionCreator

    Map<String, Closure> branchVersionCreators

    ChecksConfig checks = new ChecksConfig()

    boolean sanitizeVersion = true

    boolean createReleaseCommit = false

    Closure releaseCommitMessage = PredefinedReleaseCommitMessageCreator.DEFAULT.commitMessageCreator

    VersionService versionService

    private String resolvedVersion = null

    private VersionWithPosition rawVersion = null

    @Inject
    VersionConfig(Project project) {
        this.project = project
        this.repositoryDir = project.rootProject.file('./')
    }

    void tag(Closure c) {
        project.configure(tag, c)
    }

    void checks(Closure c) {
        project.configure(checks, c)
    }

    void versionCreator(String type) {
        this.versionCreator = PredefinedVersionCreator.versionCreatorFor(type)
    }

    void releaseCommitMessage(Closure c) {
        releaseCommitMessage = c
    }

    void createReleaseCommit(boolean createReleaseCommit) {
        this.createReleaseCommit = createReleaseCommit
    }

    void versionCreator(Closure c) {
        this.versionCreator = c
    }

    String getVersion() {
        if (resolvedVersion == null) {
            resolvedVersion = versionService.currentDecoratedVersion(this, VersionReadOptions.fromProject(project))
        }
        return resolvedVersion
    }

    VersionWithPosition getRawVersion() {
        if(rawVersion == null) {
            rawVersion = versionService.currentVersion(this, VersionReadOptions.fromProject(project))
        }
        return rawVersion
    }
}
