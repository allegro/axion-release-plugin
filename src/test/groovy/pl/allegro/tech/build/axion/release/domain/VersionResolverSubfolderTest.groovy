package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.Fixtures
import pl.allegro.tech.build.axion.release.RepositoryBasedTest
import pl.allegro.tech.build.axion.release.domain.properties.*
import pl.allegro.tech.build.axion.release.infrastructure.di.VersionResolutionContext
import spock.lang.Shared

import static pl.allegro.tech.build.axion.release.TagPrefixConf.*
import static pl.allegro.tech.build.axion.release.domain.properties.NextVersionPropertiesBuilder.nextVersionProperties
import static pl.allegro.tech.build.axion.release.domain.properties.TagPropertiesBuilder.tagProperties
import static pl.allegro.tech.build.axion.release.domain.scm.ScmPropertiesBuilder.scmProperties

class VersionResolverSubfolderTest extends RepositoryBasedTest {

    VersionResolver resolver

    TagProperties tagRules = tagProperties().build()

    NextVersionProperties nextVersionRules = nextVersionProperties().build()

    @Shared
    MonorepoConfig defaultMonorepoProperties = Fixtures.monorepoConfig()

    VersionProperties defaultMonorepoVersionRules = VersionPropertiesBuilder.versionProperties()
        .supportMonorepos(defaultMonorepoProperties)
        .build()

    String projectRootSubfolder

    def setup() {
        this.projectRootSubfolder = "gradleProjectRoot"
        resolver = new VersionResolver(repository, projectRootSubfolder)
    }

    def "should return default previous and current version when no tag in repository"() {
        given:
        createAndCommitFileInSubfolder(projectRootSubfolder, 'foo')
        configureContextWithVersionRules(defaultMonorepoVersionRules)

        when:
        VersionContext version = resolver.resolveVersion(defaultMonorepoVersionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '0.1.0'
        version.version.toString() == '0.1.0'
        version.snapshot

    }

    def "should return same previous and current version when no change in subfolder since release tag"() {
        given:
        createAndCommitFileInSubfolder(projectRootSubfolder, 'foo')
        repository.tag(fullPrefix() + '1.1.0')
        repository.commit(['*'], 'Commit without change in subfolder')
        configureContextWithVersionRules(defaultMonorepoVersionRules)

        when:
        VersionContext version = resolver.resolveVersion(defaultMonorepoVersionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '1.1.0'
        !version.snapshot
    }

    def "should pick tag with highest version when multiple tags on last commit with changes in subfolder"() {
        given:
        createAndCommitFileInSubfolder(projectRootSubfolder, 'foo')
        repository.tag(fullPrefix() + '1.0.0')
        repository.tag(fullPrefix() +'1.1.0')
        repository.tag(fullPrefix() +'1.2.0')
        repository.commit(['*'], 'Commit without change in subfolder')
        configureContextWithVersionRules(defaultMonorepoVersionRules)

        when:
        VersionContext version = resolver.resolveVersion(defaultMonorepoVersionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.2.0'
        version.version.toString() == '1.2.0'
        !version.snapshot
    }

    def "should pick tag with highest version when multiple release and non-release tags on last commit with changes in subfolder"() {
        given:
        createAndCommitFileInSubfolder(projectRootSubfolder, 'foo')
        repository.tag(fullPrefix() + '1.0.0')
        repository.tag(fullPrefix() +'1.1.0')
        repository.tag(fullPrefix() +'1.1.5-alpha')
        repository.tag(fullPrefix() +'1.2.0')
        repository.tag(fullPrefix() +'1.4.0-alpha')
        repository.commit(['*'], 'Commit without change in subfolder')
        configureContextWithVersionRules(defaultMonorepoVersionRules)

        when:
        VersionContext version = resolver.resolveVersion(defaultMonorepoVersionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.2.0'
        version.version.toString() == '1.2.0'
        !version.snapshot
    }

    def "should prefer snapshot of nextVersion when both on latest relevant commit and forceSnapshot is enabled"() {

        given: "there is releaseTag and nextVersionTag on current commit"
        createAndCommitFileInSubfolder(projectRootSubfolder, 'foo')
        repository.tag(fullPrefix() +'1.0.0')
        repository.tag(fullPrefix() +'1.1.0-alpha')
        repository.commit(['*'], 'Commit without change in subfolder')

        VersionProperties versionRules = VersionPropertiesBuilder.versionProperties()
            .supportMonorepos(defaultMonorepoProperties)
            .forceSnapshot()
            .build()
        configureContextWithVersionRules(versionRules)

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
        repository.tag(fullPrefix() +'1.1.0')
        createAndCommitFileInSubfolder(projectRootSubfolder, 'bar')

        when:
        configureContextWithVersionRules(versionRules)
        VersionContext version = resolver.resolveVersion(versionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '1.1.1'
        version.snapshot

        where:
        versionRules << [
            VersionPropertiesBuilder.versionProperties().supportMonorepos(defaultMonorepoProperties).build(),
            VersionPropertiesBuilder.versionProperties().supportMonorepos(defaultMonorepoProperties).forceSnapshot().build()
        ]
    }

    def "should return the highest version from the tagged versions, even if subfolder not changed in that commit"(VersionProperties versionRules) {
        given:
        createAndCommitFileInSubfolder(projectRootSubfolder, 'foo')
        repository.tag(fullPrefix() +'1.0.0')
        repository.commit(['*'], 'Commit without change in subfolder')
        repository.tag(fullPrefix() +'1.5.0')
        createAndCommitFileInSubfolder(projectRootSubfolder, 'bar')
        repository.tag(fullPrefix() +'1.2.0')
        createAndCommitFileInSubfolder(projectRootSubfolder, 'bar2')
        repository.tag(fullPrefix() +'1.3.0')

        when:
        configureContextWithVersionRules(versionRules)
        VersionContext version = resolver.resolveVersion(versionRules, tagRules, nextVersionRules)
        println "Version Resolved: $version"

        then:
        version.previousVersion.toString() == '1.5.0'
        version.version.toString() == '1.5.1'
        version.snapshot

        where:
        versionRules << [
            VersionPropertiesBuilder.versionProperties().supportMonorepos(defaultMonorepoProperties).useHighestVersion().build(),
            VersionPropertiesBuilder.versionProperties().supportMonorepos(defaultMonorepoProperties).useHighestVersion().forceSnapshot().build()
        ]
    }

    def "should return the highest version from the tagged versions, even if subfolder not changed in that commit, when not on release"(VersionProperties versionRules) {
        given:
        createAndCommitFileInSubfolder(projectRootSubfolder, 'foo')
        repository.tag(fullPrefix() + '1.0.0')
        repository.commit(['*'], 'Commit without change in subfolder')
        repository.tag(fullPrefix() +'1.5.0')
        createAndCommitFileInSubfolder(projectRootSubfolder, 'bar')
        repository.tag(fullPrefix() + '1.2.0')
        createAndCommitFileInSubfolder(projectRootSubfolder, 'bar2')
        repository.tag(fullPrefix() + '1.3.0')
        repository.commit(['*'], 'Commit without change in subfolder')

        when:
        configureContextWithVersionRules(versionRules)
        VersionContext version = resolver.resolveVersion(versionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.5.0'
        version.version.toString() == '1.5.1'
        version.snapshot

        where:
        versionRules << [
            VersionPropertiesBuilder.versionProperties().supportMonorepos(defaultMonorepoProperties).useHighestVersion().build(),
            VersionPropertiesBuilder.versionProperties().supportMonorepos(defaultMonorepoProperties).useHighestVersion().forceSnapshot().build()
        ]
    }

    private void configureContextWithVersionRules(VersionProperties versionRules) {
        context = new VersionResolutionContext(
            PropertiesBuilder.properties().withVersionRules(versionRules).build(),
            context.repository(),
            scmProperties(temporaryFolder).build(),
            temporaryFolder,
            new LocalOnlyResolver(true)
        )

        repository = context.repository()
    }

    private void createAndCommitFileInSubfolder(String path, String filename) {
        def subfolder = new File(temporaryFolder, path)
        def dummyFile = new File(temporaryFolder, "${path}/${filename}")
        subfolder.mkdirs()
        dummyFile.createNewFile()
        repository.commit(['.'], "create file ${path}/${filename}")
    }
}
