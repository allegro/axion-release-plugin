package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.TaskOutcome

import java.util.stream.Collectors

class MultiModuleProjectDependencyIntegrationTest extends BaseIntegrationTest {

    File getSubmoduleDir(String projectName) {
        return new File(temporaryFolder.root, projectName)
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
     * We start off with v1.0.0 on the parent project and versions 2.0.0, 3.0.0 and 4.0.0 for the 3 child projects.
     * project2 depends on project1, project3 depends on project2
     */
    void initialProjectConfiguration() {
        List<String> submodules = ["project1", "project2", "project3"]
        buildFile('''
        plugins {
            id 'java'
        }
        scmVersion {
            monorepos {
                projectDirs = project.subprojects.collect({p -> p.name})
            }
        }
        subprojects {
            apply plugin: 'java'
        }
        project(":project2") {
            dependencies {
                implementation project(':project1')
            }
        }
        project(":project3") {
            dependencies {
                implementation project(':project2')
            }
        }
        '''
        )
        generateSettingsFile(temporaryFolder.root, submodules)
        generateGitIgnoreFile(temporaryFolder.root)

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

        when:
        result = runGradle(':project3:currentVersion')
        match = result.output =~ /(?m)^.*Project version: (.*)$/

        then:
        match[0][1] == '4.0.0'
        result.task(":project3:currentVersion").outcome == TaskOutcome.SUCCESS

    }

    def "releaseDependents and change to dependent submodule should not change root or upstream submodule versions"() {
        given:
        initialProjectConfiguration()
        File dummy = new File(getSubmoduleDir("project2"), "dummy.txt")
        dummy.createNewFile()
        repository.commit(["project2"], "commit submodule project2")
        runGradle(":project2:releaseDependents", "-Prelease.version=5.0.0", '-Prelease.localOnly', '-Prelease.disableChecks')

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
        match[0][1] == '5.0.0'
        result.task(":project2:currentVersion").outcome == TaskOutcome.SUCCESS

    }

   def "releaseDependents and change to upstream submodule should force increment dependent module version despite no changes there"() {
        given:
        initialProjectConfiguration()
        File dummy = new File(getSubmoduleDir("project1"), "dummy.txt")
        dummy.createNewFile()
        repository.commit(["project1"], "commit submodule project1")
        runGradle(":project1:releaseDependents", '-Prelease.localOnly', '-Prelease.disableChecks')

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
        match[0][1] == '2.0.1'
        result.task(":project1:currentVersion").outcome == TaskOutcome.SUCCESS

        when:
        result = runGradle(':project2:currentVersion')
        match = result.output =~ /(?m)^.*Project version: (.*)$/

        then:
        match[0][1] == '3.0.1'
        result.task(":project2:currentVersion").outcome == TaskOutcome.SUCCESS

        when:
        result = runGradle(':project3:currentVersion')
        match = result.output =~ /(?m)^.*Project version: (.*)$/

        then:
        match[0][1] == '4.0.1'
        result.task(":project3:currentVersion").outcome == TaskOutcome.SUCCESS

    }

    def "releaseDependents and change to submodule should not change root project version"() {
        given:
        initialProjectConfiguration()
        File dummy = new File(getSubmoduleDir("project1"), "dummy.txt")
        dummy.createNewFile()
        repository.commit(["project1"], "commit submodule project1")
        runGradle(":project1:releaseDependents", "-Prelease.version=4.0.0", '-Prelease.localOnly', '-Prelease.disableChecks')

        when:
        def result = runGradle(':currentVersion')
        def match = result.output =~ /(?m)^.*Project version: (.*)$/

        then:
        match[0][1] == '1.0.0'
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }

    def "releaseDependents and change to root project should not change child project versions"() {
        given:
        initialProjectConfiguration()
        File dummy = new File(temporaryFolder.root, "dummy.txt")
        dummy.createNewFile()
        repository.commit(["dummy.txt"], "commit parent project")
        runGradle(":releaseDependents", "-Prelease.version=4.0.0", '-Prelease.localOnly', '-Prelease.disableChecks')

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

        when:
        result = runGradle(':project3:currentVersion')
        match = result.output =~ /(?m)^.*Project version: (.*)$/

        then:
        match[0][1] == '4.0.0'
        result.task(":project3:currentVersion").outcome == TaskOutcome.SUCCESS

    }

    def "createReleaseDependents and change to dependent submodule should not change root or upstream submodule versions"() {
        given:
        initialProjectConfiguration()
        File dummy = new File(getSubmoduleDir("project2"), "dummy.txt")
        dummy.createNewFile()
        repository.commit(["project2"], "commit submodule project2")
        runGradle(":project2:createReleaseDependents", "-Prelease.version=5.0.0", '-Prelease.disableChecks')

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
        match[0][1] == '5.0.0'
        result.task(":project2:currentVersion").outcome == TaskOutcome.SUCCESS

    }

    def "createReleaseDependents and change to upstream submodule should force increment dependent module version despite no changes there"() {
        given:
        initialProjectConfiguration()
        File dummy = new File(getSubmoduleDir("project1"), "dummy.txt")
        dummy.createNewFile()
        repository.commit(["project1"], "commit submodule project1")
        runGradle(":project1:createReleaseDependents", '-Prelease.disableChecks')

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
        match[0][1] == '2.0.1'
        result.task(":project1:currentVersion").outcome == TaskOutcome.SUCCESS

        when:
        result = runGradle(':project2:currentVersion')
        match = result.output =~ /(?m)^.*Project version: (.*)$/

        then:
        match[0][1] == '3.0.1'
        result.task(":project2:currentVersion").outcome == TaskOutcome.SUCCESS

        when:
        result = runGradle(':project3:currentVersion')
        match = result.output =~ /(?m)^.*Project version: (.*)$/

        then:
        match[0][1] == '4.0.1'
        result.task(":project3:currentVersion").outcome == TaskOutcome.SUCCESS

    }

    def "createReleaseDependents and change to submodule should not change root project version"() {
        given:
        initialProjectConfiguration()
        File dummy = new File(getSubmoduleDir("project1"), "dummy.txt")
        dummy.createNewFile()
        repository.commit(["project1"], "commit submodule project1")
        runGradle(":project1:createReleaseDependents", "-Prelease.version=4.0.0", '-Prelease.disableChecks')

        when:
        def result = runGradle(':currentVersion')
        def match = result.output =~ /(?m)^.*Project version: (.*)$/

        then:
        match[0][1] == '1.0.0'
        result.task(":currentVersion").outcome == TaskOutcome.SUCCESS
    }

    def "createReleaseDependents and change to root project should not change child project versions"() {
        given:
        initialProjectConfiguration()
        File dummy = new File(temporaryFolder.root, "dummy.txt")
        dummy.createNewFile()
        repository.commit(["dummy.txt"], "commit parent project")
        runGradle(":createReleaseDependents", "-Prelease.version=4.0.0", '-Prelease.disableChecks')

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

        when:
        result = runGradle(':project3:currentVersion')
        match = result.output =~ /(?m)^.*Project version: (.*)$/

        then:
        match[0][1] == '4.0.0'
        result.task(":project3:currentVersion").outcome == TaskOutcome.SUCCESS

    }
}
