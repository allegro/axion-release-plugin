package pl.allegro.tech.build.axion.release

import org.gradle.testkit.runner.TaskOutcome

class ChecksPropertiesIntegrationTest extends BaseIntegrationTest {

    def "should return default value from config when no project properties are present"() {
        given:
        buildFile('''
            scmVersion {
                checks {
                    aheadOfRemote = true
                    uncommittedChanges = true
                    snapshotDependencies = true
                }
            }

            task verifyChecks {
                doLast {
                    println "aheadOfRemote: ${scmVersion.checks.checkAheadOfRemote().get()}"
                    println "uncommittedChanges: ${scmVersion.checks.checkUncommittedChanges().get()}"
                    println "snapshotDependencies: ${scmVersion.checks.checkSnapshotDependencies().get()}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyChecks')

        then:
        result.task(":verifyChecks").outcome == TaskOutcome.SUCCESS
        result.output.contains("aheadOfRemote: true")
        result.output.contains("uncommittedChanges: true")
        result.output.contains("snapshotDependencies: true")
    }

    def "should always return false if checks are globally disabled using release.disableChecks"() {
        given:
        buildFile('''
            scmVersion {
                checks {
                    aheadOfRemote = true
                    uncommittedChanges = true
                    snapshotDependencies = true
                }
            }

            task verifyChecks {
                doLast {
                    println "aheadOfRemote: ${scmVersion.checks.checkAheadOfRemote().get()}"
                    println "uncommittedChanges: ${scmVersion.checks.checkUncommittedChanges().get()}"
                    println "snapshotDependencies: ${scmVersion.checks.checkSnapshotDependencies().get()}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyChecks', '-Prelease.disableChecks')

        then:
        result.task(":verifyChecks").outcome == TaskOutcome.SUCCESS
        result.output.contains("aheadOfRemote: false")
        result.output.contains("uncommittedChanges: false")
        result.output.contains("snapshotDependencies: false")
    }

    def "should skip uncommitted changes check if it was disabled using project property"() {
        given:
        buildFile('''
            scmVersion {
                checks {
                    uncommittedChanges = true
                    aheadOfRemote = true
                    snapshotDependencies = true
                }
            }

            task verifyChecks {
                doLast {
                    println "uncommittedChanges: ${scmVersion.checks.checkUncommittedChanges().get()}"
                    println "aheadOfRemote: ${scmVersion.checks.checkAheadOfRemote().get()}"
                    println "snapshotDependencies: ${scmVersion.checks.checkSnapshotDependencies().get()}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyChecks', '-Prelease.disableUncommittedCheck')

        then:
        result.task(":verifyChecks").outcome == TaskOutcome.SUCCESS
        result.output.contains("uncommittedChanges: false")
        result.output.contains("aheadOfRemote: true")
        result.output.contains("snapshotDependencies: true")
    }

    def "should skip ahead of remote check if it was disabled using project property"() {
        given:
        buildFile('''
            scmVersion {
                checks {
                    aheadOfRemote = true
                    uncommittedChanges = true
                    snapshotDependencies = true
                }
            }

            task verifyChecks {
                doLast {
                    println "aheadOfRemote: ${scmVersion.checks.checkAheadOfRemote().get()}"
                    println "uncommittedChanges: ${scmVersion.checks.checkUncommittedChanges().get()}"
                    println "snapshotDependencies: ${scmVersion.checks.checkSnapshotDependencies().get()}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyChecks', '-Prelease.disableRemoteCheck')

        then:
        result.task(":verifyChecks").outcome == TaskOutcome.SUCCESS
        result.output.contains("aheadOfRemote: false")
        result.output.contains("uncommittedChanges: true")
        result.output.contains("snapshotDependencies: true")
    }

    def "should skip snapshots check if it was disabled using project property"() {
        given:
        buildFile('''
            scmVersion {
                checks {
                    snapshotDependencies = true
                    aheadOfRemote = true
                    uncommittedChanges = true
                }
            }

            task verifyChecks {
                doLast {
                    println "snapshotDependencies: ${scmVersion.checks.checkSnapshotDependencies().get()}"
                    println "aheadOfRemote: ${scmVersion.checks.checkAheadOfRemote().get()}"
                    println "uncommittedChanges: ${scmVersion.checks.checkUncommittedChanges().get()}"
                }
            }
        ''')

        when:
        def result = runGradle('verifyChecks', '-Prelease.disableSnapshotsCheck')

        then:
        result.task(":verifyChecks").outcome == TaskOutcome.SUCCESS
        result.output.contains("snapshotDependencies: false")
        result.output.contains("aheadOfRemote: true")
        result.output.contains("uncommittedChanges: true")
    }

}
