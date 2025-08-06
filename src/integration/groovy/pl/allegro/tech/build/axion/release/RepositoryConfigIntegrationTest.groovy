package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.TaskOutcome

class RepositoryConfigIntegrationTest extends BaseIntegrationTest {

    def "should set repository directory to rootProject dir by default"() {
        given:
        buildFile('''
            task verifyRepositoryConfig {
                doLast {
                    println "directory: ${scmVersion.repository.directory.get()}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyRepositoryConfig')

        then:
        result.task(":verifyRepositoryConfig").outcome == TaskOutcome.SUCCESS
        // The output might contain /private prefix on macOS
        result.output.contains(temporaryFolder.absolutePath) ||
            result.output.contains("/private" + temporaryFolder.absolutePath)
    }

    def "should not initialize authorization options when no flags on project"() {
        given:
        buildFile('''
            task verifyRepositoryConfig {
                doLast {
                    println "customKey present: ${scmVersion.repository.customKey.isPresent()}"
                    println "customKeyPassword present: ${scmVersion.repository.customKeyPassword.isPresent()}"
                    println "customUsername present: ${scmVersion.repository.customUsername.isPresent()}"
                    println "customPassword present: ${scmVersion.repository.customPassword.isPresent()}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyRepositoryConfig')

        then:
        result.task(":verifyRepositoryConfig").outcome == TaskOutcome.SUCCESS
        result.output.contains("customKey present: false")
        result.output.contains("customKeyPassword present: false")
        result.output.contains("customUsername present: false")
        result.output.contains("customPassword present: false")
    }

    def "should set authorization options when custom key and password provided"() {
        given:
        buildFile('''
            task verifyRepositoryConfig {
                doLast {
                    println "customKey: ${scmVersion.repository.customKey.orNull}"
                    println "customKeyPassword: ${scmVersion.repository.customKeyPassword.orNull}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyRepositoryConfig', '-Prelease.customKey=key', '-Prelease.customKeyPassword=password')

        then:
        result.task(":verifyRepositoryConfig").outcome == TaskOutcome.SUCCESS
        result.output.contains("customKey: key")
        result.output.contains("customKeyPassword: password")
    }

    def "should set username and password when provided via properties"() {
        given:
        buildFile('''
            task verifyRepositoryConfig {
                doLast {
                    println "customUsername: ${scmVersion.repository.customUsername.orNull}"
                    println "customPassword: ${scmVersion.repository.customPassword.orNull}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyRepositoryConfig', '-Prelease.customUsername=username', '-Prelease.customPassword=password')

        then:
        result.task(":verifyRepositoryConfig").outcome == TaskOutcome.SUCCESS
        result.output.contains("customUsername: username")
        result.output.contains("customPassword: password")
    }

    def "should read key from file when customKeyFile property used"() {
        given:
        def keyFile = newFile("keyFile")
        keyFile.text = "keyFileContent"

        buildFile('''
            task verifyRepositoryConfig {
                doLast {
                    println "customKeyFile: ${scmVersion.repository.customKeyFile.asFile.orNull?.name}"
                    println "customKeyPassword: ${scmVersion.repository.customKeyPassword.orNull}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyRepositoryConfig', '-Prelease.customKeyFile=keyFile', '-Prelease.customKeyPassword=password')

        then:
        result.task(":verifyRepositoryConfig").outcome == TaskOutcome.SUCCESS
        result.output.contains("customKeyFile: keyFile")
        result.output.contains("customKeyPassword: password")
    }

    def "should prefer explicit custom key before key read from file when both properties used"() {
        given:
        def keyFile = newFile("keyFile")
        keyFile.text = "keyFileContent"

        buildFile('''
            task verifyRepositoryConfig {
                doLast {
                    println "customKey: ${scmVersion.repository.customKey.orNull}"
                    println "customKeyFile: ${scmVersion.repository.customKeyFile.asFile.orNull?.name}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyRepositoryConfig',
            '-Prelease.customKey=explicitKey',
            '-Prelease.customKeyFile=keyFile',
            '-Prelease.customKeyPassword=password')

        then:
        result.task(":verifyRepositoryConfig").outcome == TaskOutcome.SUCCESS
        result.output.contains("customKey: explicitKey")
        result.output.contains("customKeyFile: keyFile")
    }
}
