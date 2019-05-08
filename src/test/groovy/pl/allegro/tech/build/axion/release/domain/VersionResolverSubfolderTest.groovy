package pl.allegro.tech.build.axion.release.domain


import pl.allegro.tech.build.axion.release.RepositoryBasedTest
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties

import static pl.allegro.tech.build.axion.release.domain.properties.NextVersionPropertiesBuilder.nextVersionProperties
import static pl.allegro.tech.build.axion.release.domain.properties.TagPropertiesBuilder.tagProperties
import static pl.allegro.tech.build.axion.release.domain.properties.VersionPropertiesBuilder.versionProperties

class VersionResolverSubfolderTest extends RepositoryBasedTest {

    VersionResolver resolver

    TagProperties tagRules = tagProperties().build()

    NextVersionProperties nextVersionRules = nextVersionProperties().build()

    VersionProperties defaultVersionRules = versionProperties().build()

    String projectRootSubfolder

    def setup() {
        this.projectRootSubfolder = "gradleProjectRoot"
        resolver = new VersionResolver(repository, projectRootSubfolder)
    }

    def "should return default previous and current version when no tag in repository"() {
        given:
        createAndCommitFileInSubfolder(projectRootSubfolder, 'foo')

        when:
        VersionContext version = resolver.resolveVersion(defaultVersionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '0.1.0'
        version.version.toString() == '0.1.0'
        version.snapshot

    }

    def "should return same previous and current version when no change in subfolder since release tag"() {
        given:
        createAndCommitFileInSubfolder(projectRootSubfolder, 'foo')
        repository.tag('release-1.1.0')
        repository.commit(['*'], 'Commit without change in subfolder')

        when:
        VersionContext version = resolver.resolveVersion(defaultVersionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '1.1.0'
        !version.snapshot
    }

    def "should pick tag with highest version when multiple tags on last commit with changes in subfolder"() {
        given:
        createAndCommitFileInSubfolder(projectRootSubfolder, 'foo')
        repository.tag('release-1.0.0')
        repository.tag('release-1.1.0')
        repository.tag('release-1.2.0')
        repository.commit(['*'], 'Commit without change in subfolder')

        when:
        VersionContext version = resolver.resolveVersion(defaultVersionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.2.0'
        version.version.toString() == '1.2.0'
        !version.snapshot
    }

    def "should pick tag with highest version when multiple release and non-release tags on last commit with changes in subfolder"() {
        given:
        createAndCommitFileInSubfolder(projectRootSubfolder, 'foo')
        repository.tag('release-1.0.0')
        repository.tag('release-1.1.0')
        repository.tag('release-1.1.5-alpha')
        repository.tag('release-1.2.0')
        repository.tag('release-1.4.0-alpha')
        repository.commit(['*'], 'Commit without change in subfolder')

        when:
        VersionContext version = resolver.resolveVersion(defaultVersionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.2.0'
        version.version.toString() == '1.2.0'
        !version.snapshot
    }

    def "should prefer snapshot of nextVersion when both on latest relevant commit and forceSnapshot is enabled"() {

        given: "there is releaseTag and nextVersionTag on current commit"
        createAndCommitFileInSubfolder(projectRootSubfolder, 'foo')
        repository.tag('release-1.0.0')
        repository.tag('release-1.1.0-alpha')
        repository.commit(['*'], 'Commit without change in subfolder')
        VersionProperties versionRules = versionProperties().forceSnapshot().build()

        when: "resolving version with property 'release.forceSnapshot'"
        VersionContext version = resolver.resolveVersion(versionRules, tagRules, nextVersionRules)

        then: "the resolved version should be snapshot towards the next version"
        version.previousVersion.toString() == '1.0.0'
        version.version.toString() == '1.1.0'
        version.snapshot
    }

    def "should return unmodified previous and incremented current version when changes in subfolder since tag"(VersionProperties versionRules) {
        given:
        createAndCommitFileInSubfolder(projectRootSubfolder, 'foo')
        repository.tag('release-1.1.0')
        createAndCommitFileInSubfolder(projectRootSubfolder, 'bar')

        when:
        VersionContext version = resolver.resolveVersion(versionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '1.1.1'
        version.snapshot

        where:
        versionRules << [
            versionProperties().build(),
            versionProperties().forceSnapshot().build()
        ]
    }

    def "should return the highest version from the tagged versions, even if subfolder not changed in that commit"(VersionProperties versionProps) {
        given:
        createAndCommitFileInSubfolder(projectRootSubfolder, 'foo')
        repository.tag('release-1.0.0')
        repository.commit(['*'], 'Commit without change in subfolder')
        repository.tag('release-1.5.0')
        createAndCommitFileInSubfolder(projectRootSubfolder, 'bar')
        repository.tag('release-1.2.0')
        createAndCommitFileInSubfolder(projectRootSubfolder, 'bar2')
        repository.tag('release-1.3.0')

        when:
        VersionContext version = resolver.resolveVersion(versionProps, tagRules, nextVersionRules)
        println "Version Resolved: $version"

        then:
        version.previousVersion.toString() == '1.5.0'
        version.version.toString() == '1.5.1'
        version.snapshot

        where:
        versionProps << [
            versionProperties().useHighestVersion().build(),
            versionProperties().useHighestVersion().forceSnapshot().build()
        ]
    }

    def "should return the highest version from the tagged versions, even if subfolder not changed in that commit, when not on release"(VersionProperties versionProps) {
        given:
        createAndCommitFileInSubfolder(projectRootSubfolder, 'foo')
        repository.tag('release-1.0.0')
        repository.commit(['*'], 'Commit without change in subfolder')
        repository.tag('release-1.5.0')
        createAndCommitFileInSubfolder(projectRootSubfolder, 'bar')
        repository.tag('release-1.2.0')
        createAndCommitFileInSubfolder(projectRootSubfolder, 'bar2')
        repository.tag('release-1.3.0')
        repository.commit(['*'], 'Commit without change in subfolder')

        when:
        VersionContext version = resolver.resolveVersion(versionProps, tagRules, nextVersionRules)
        println "Version Resolved: $version"

        then:
        version.previousVersion.toString() == '1.5.0'
        version.version.toString() == '1.5.1'
        version.snapshot

        where:
        versionProps << [
            versionProperties().useHighestVersion().build(),
            versionProperties().useHighestVersion().forceSnapshot().build()
        ]
    }

    // TODO add missing test cases (see VersionResolverTest as a reference for potential test cases)

    private void createAndCommitFileInSubfolder(String path, String filename) {
        def subfolder = new File(directory, path)
        def dummyFile = new File(directory, "${path}/${filename}")
        subfolder.mkdirs()
        dummyFile.createNewFile()
        repository.commit(['.'], "create file ${path}/${filename}")
    }
}
