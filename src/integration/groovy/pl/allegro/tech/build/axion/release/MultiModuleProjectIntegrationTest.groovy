package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.TaskOutcome

import java.util.stream.Collectors

class MultiModuleProjectIntegrationTest extends BaseIntegrationTest {

    File getSubmoduleDir(String projectName) {
        return new File(temporaryFolder, projectName)
    }

    void generateSubmoduleBuildFile(String projectName) {
        File submoduleDir = getSubmoduleDir(projectName)
        submoduleDir.mkdirs()
        File buildFile = new File(submoduleDir, "build.gradle")
        buildFile << """
        plugins {
            id 'pl.allegro.tech.build.axion-release'
        }

        scmVersion {
            tag {
                prefix = '${projectName}'
            }
        }

        project.version = scmVersion.version
        """
    }

    void generateSettingsFile(File dir, List<String> submodules) {
        String submodulesIncludeString = submodules.stream().map({ m -> "'${m}'" }).collect(Collectors.joining(','))
        File settings = new File(dir, "settings.gradle")
        settings << """
        include ${submodulesIncludeString}

        """
    }

    void generateGitIgnoreFile(File dir) {
        File gitIgnore = new File(dir, ".gitignore")
        gitIgnore << """\
        .gradle
        """.stripIndent()
    }

    /**
     * Configure the multi-module project for testing.
     * We start off with v1.0.0 on the parent project and versions 2.0.0 and 3.0.0 for the 2 child projects.
     */
    void initialProjectConfiguration() {
        List<String> submodules = ["project1", "project2"]
        buildFile('''
        scmVersion {
            monorepos {
                exclude(project.subprojects.collect({p -> p.name}))
            }
        }
        '''
        )
        generateSettingsFile(temporaryFolder, submodules)
        generateGitIgnoreFile(temporaryFolder)

        repository.commit(['.'], "initial commit of top level project")

        // create version for main project
        runGradle(':createRelease', '-Prelease.version=1.0.0', '-Prelease.disableChecks')

        // create submodules
        int versionCounter = 2
        for (String module : submodules) {
            // generate the project files
            generateSubmoduleBuildFile(module)
            // add to repo
            repository.commit(["${module}".toString()], "commit submodule ${module}".toString())
            // tag release for that project
            runGradle(":${module}:createRelease", "-Prelease.version=${versionCounter}.0.0", '-Prelease.disableChecks')
            versionCounter++
        }
    }

    def "plugin can distinguish between submodules versions"() {
        given:
        initialProjectConfiguration()

        when:
        def result = runGradle(':currentVersion')
        def match = result.output =~ /(?m)^.*Project version: (.*)$/

        then:
        match[0][1] == '1.0.0'
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS

        when:
        result = runGradle(':project1:currentVersion')
        match = result.output =~ /(?m)^.*Project version: (.*)$/

        then:
        match[0][1] == '2.0.0'
        result.task(":project1:currentVersion").outcome == TaskOutcome.SUCCESS

        when:
        result = runGradle(':project2:currentVersion')
        match = result.output =~ /(?m)^.*Project version: (.*)$/

        then:
        match[0][1] == '3.0.0'
        result.task(":project2:currentVersion").outcome == TaskOutcome.SUCCESS

    }

    def "change to submodule should not change sibling project version"() {
        given:
        initialProjectConfiguration()
        File dummy = new File(getSubmoduleDir("project1"), "dummy.txt")
        dummy.createNewFile()
        repository.commit(["project1"], "commit submodule project1")
        runGradle(":project1:release", "-Prelease.version=4.0.0", '-Prelease.localOnly', '-Prelease.disableChecks')

        when:
        def result = runGradle(':project1:currentVersion')
        def match = result.output =~ /(?m)^.*Project version: (.*)$/

        then:
        match[0][1] == '4.0.0'
        result.task(":project1:currentVersion").outcome == TaskOutcome.SUCCESS

        when:
        result = runGradle(':project2:currentVersion')
        match = result.output =~ /(?m)^.*Project version: (.*)$/

        then:
        match[0][1] == '3.0.0'
        result.task(":project2:currentVersion").outcome == TaskOutcome.SUCCESS

    }

    def "change to submodule should not change root project version"() {
        given:
        initialProjectConfiguration()
        File dummy = new File(getSubmoduleDir("project1"), "dummy.txt")
        dummy.createNewFile()
        repository.commit(["project1"], "commit submodule project1")
        runGradle(":project1:release", "-Prelease.version=4.0.0", '-Prelease.localOnly', '-Prelease.disableChecks')

        when:
        def result = runGradle(':currentVersion')
        def match = result.output =~ /(?m)^.*Project version: (.*)$/

        then:
        match[0][1] == '1.0.0'
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }

    def "change to root project should not change child project versions"() {
        given:
        initialProjectConfiguration()
        File dummy = new File(temporaryFolder, "dummy.txt")
        dummy.createNewFile()
        repository.commit(["dummy.txt"], "commit parent project")
        runGradle(":release", "-Prelease.version=4.0.0", '-Prelease.localOnly', '-Prelease.disableChecks')

        when:
        def result = runGradle(':currentVersion')
        def match = result.output =~ /(?m)^.*Project version: (.*)$/

        then:
        match[0][1] == '4.0.0'
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS

        when:
        result = runGradle(':project1:currentVersion')
        match = result.output =~ /(?m)^.*Project version: (.*)$/

        then:
        match[0][1] == '2.0.0'
        result.task(":project1:currentVersion").outcome == TaskOutcome.SUCCESS

        when:
        result = runGradle(':project2:currentVersion')
        match = result.output =~ /(?m)^.*Project version: (.*)$/

        then:
        match[0][1] == '3.0.0'
        result.task(":project2:currentVersion").outcome == TaskOutcome.SUCCESS

    }
}
