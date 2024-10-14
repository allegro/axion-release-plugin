package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.TaskOutcome

class VerifyReleaseIntegrationTest extends BaseIntegrationTest {

    def "should print changes in Git as seen by axion-release"() {
        given:
        buildFile('')
        new FileTreeBuilder(temporaryFolder).file('my-uncommitted-file', "hello")

        when:
        def result = runGradle('verifyRelease', '-Prelease.dryRun', '-Prelease.localOnly')

        then:
        result.output.contains("Unstaged changes:")
        result.output.contains('my-uncommitted-file')
        result.task(":verifyRelease").outcome == TaskOutcome.SUCCESS
    }

    def "should work in multimodule project setup versioning every module separately"() {
        given:
        initialMultiModuleMultiVersionProjectConfiguration()

        when:
        def result = runGradle(':verifyRelease')

        then:
        result.task(":verifyRelease").outcome == TaskOutcome.SUCCESS
    }

    def "should work in multimodule project setup with the same version for every module"() {
        given:
        initialMultiModuleSingleVersionProjectConfiguration()

        when:
        def result = runGradle(':verifyRelease')

        then:
        result.task(":verifyRelease").outcome == TaskOutcome.SUCCESS
    }

    void initialMultiModuleSingleVersionProjectConfiguration() {
        buildFile('''
            apply plugin: 'java'

            allprojects {
                version = scmVersion.version
            }

            dependencies {
                 implementation(project(":module1"))
            }
        ''')
        generateSettingsFile(temporaryFolder)
        generateGitIgnoreFile(temporaryFolder)
        generateSubmoduleBuildFile("module1")
        repository.commit(['.'], "initial commit of top level project")
    }

    void initialMultiModuleMultiVersionProjectConfiguration() {
        buildFile('''
            apply plugin: 'java'

            allprojects {
                scmVersion {
                    tag {
                        prefix = name
                    }
                }
                version = scmVersion.version
            }

            dependencies {
                 implementation(project(":module1"))
            }
        ''')
        generateSettingsFile(temporaryFolder)
        generateGitIgnoreFile(temporaryFolder)
        generateSubmoduleBuildFile("module1")
        repository.commit(['.'], "initial commit of top level project")
    }

    void generateSubmoduleBuildFile(String projectName) {
        File submoduleDir = new File(temporaryFolder, projectName)
        submoduleDir.mkdirs()
        File buildFile = new File(submoduleDir, "build.gradle")
        buildFile << '''
        plugins {
            id 'java-platform'
        }
        '''
    }

    void generateSettingsFile(File dir) {
        File settings = new File(dir, "settings.gradle")
        settings << """
        rootProject.name = "multimodule-project"
        include(':module1')
        """
    }

    void generateGitIgnoreFile(File dir) {
        File gitIgnore = new File(dir, ".gitignore")
        gitIgnore << """\
        .gradle
        """.stripIndent()
    }
}
