package pl.allegro.tech.build.axion.release.domain

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.MergeCommand
import pl.allegro.tech.build.axion.release.RepositoryBasedTest
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties

import static pl.allegro.tech.build.axion.release.TagPrefixConf.fullPrefix
import static pl.allegro.tech.build.axion.release.domain.properties.NextVersionPropertiesBuilder.nextVersionProperties
import static pl.allegro.tech.build.axion.release.domain.properties.TagPropertiesBuilder.tagProperties
import static pl.allegro.tech.build.axion.release.domain.properties.VersionPropertiesBuilder.versionProperties

class VersionResolverMonoRepoTest extends RepositoryBasedTest {

    VersionResolver resolver

    TagProperties tagRules = tagProperties().build()

    NextVersionProperties nextVersionRules = nextVersionProperties().build()

    VersionProperties defaultVersionRules = versionProperties().build()

    String subDir = "subProjectMain"
    String secondaryDir = "subProjectSecondary"
    public static final String MASTER_BRANCH = "master"

    def setup() {
        resolver = new VersionResolver(repository, subDir)
        commitPrimaryDir("init_file")
    }

    private void commitPrimaryDir(String fileName) {
        commitFile(subDir, fileName);
    }

    private void commitNonPrimaryDir(String fileName) {
        commitFile(secondaryDir, fileName);
    }

    private void commitFile(String subDir, String fileName) {
        String fileInA = "${subDir}/${fileName}"
        new File(temporaryFolder, subDir).mkdirs()
        new File(temporaryFolder, fileInA).createNewFile()
        repository.commit([fileInA], "Add file ${fileName} in ${subDir}")
    }

    def "Should detect change and mark as snapshot"() {
        given:
        VersionProperties versionRules = versionProperties().build()
        commitPrimaryDir("1")
        repository.tag(fullPrefix() +'1.1.0')
        commitPrimaryDir("2")

        when:
        VersionContext version = resolver.resolveVersion(versionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '1.1.1'
        version.snapshot
    }

    def "should not detect change for different directories"() {
        given:
        VersionProperties versionRules = versionProperties().build()
        commitPrimaryDir("1")
        commitNonPrimaryDir("2")
        repository.tag(fullPrefix() +'1.1.0')
        commitNonPrimaryDir("3")

        when:
        VersionContext version = resolver.resolveVersion(versionRules, tagRules, nextVersionRules)
        System.out.println("asdf")

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '1.1.0'
        !version.snapshot
    }


    def "Tag and change same commit"() {
        given:
        VersionProperties versionRules = versionProperties().build()
        commitPrimaryDir("1")
        repository.tag(fullPrefix() +'1.1.0')

        when:
        VersionContext version = resolver.resolveVersion(versionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '1.1.0'
        !version.snapshot
    }

    def "Commits outside subDir shouldnt affect version"() {
        given:
        VersionProperties versionRules = versionProperties().build()
        commitPrimaryDir("1")
        repository.tag(fullPrefix() +'1.1.0')
        commitNonPrimaryDir("2")
        commitNonPrimaryDir("3")
        commitNonPrimaryDir("4")

        when:
        VersionContext version = resolver.resolveVersion(versionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '1.1.0'
        !version.snapshot
    }

    def "test force snapshot version increase"() {
        given:
        VersionProperties versionRules = versionProperties().forceSnapshot().build()
        repository.tag(fullPrefix() +'1.1.0')
        commitNonPrimaryDir('2')

        when:
        VersionContext version = resolver.resolveVersion(versionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '1.1.1'
        version.snapshot
    }

    def "test merged changes type: commit to master right before merge"() {
        given:
        VersionProperties versionRules = versionProperties().build()
        repository.tag(fullPrefix() +'1.1.0')
        commitNonPrimaryDir('1')

        Git git = repository.getJgitRepository();
        String branchName = "feature/important_changes";
        git.branchCreate().setName(branchName).call()
        commitPrimaryDir('4') // Commit to master b4 branch commit
        git.checkout().setName(branchName).call()
        commitPrimaryDir('2')
        commitNonPrimaryDir('3')
        git.checkout().setName(MASTER_BRANCH).call()

        git.merge().include(git.repository.resolve(branchName)).setCommit(true).setMessage("important").setFastForward(MergeCommand.FastForwardMode.NO_FF).call()
        commitNonPrimaryDir('5')
        repository.tag(fullPrefix() +'1.2.0')
        commitNonPrimaryDir('6')

        when:
        VersionContext version = resolver.resolveVersion(versionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.2.0'
        version.version.toString() == '1.2.0'
        !version.snapshot
    }

    def "test merged changes type: commit after branch creation"() {
        given:
        VersionProperties versionRules = versionProperties().build()
        repository.tag(fullPrefix() +'1.1.0')
        commitNonPrimaryDir('1')

        Git git = repository.getJgitRepository();
        String branchName = "feature/important_changes";
        git.branchCreate().setName(branchName).call()
        git.checkout().setName(branchName).call()
        commitPrimaryDir('2')
        commitNonPrimaryDir('3')
        git.checkout().setName(MASTER_BRANCH).call()

        commitPrimaryDir('4') // Commit to
        git.merge().include(git.repository.resolve(branchName)).setCommit(true).setMessage("important").setFastForward(MergeCommand.FastForwardMode.NO_FF).call()
        commitNonPrimaryDir('5')
        repository.tag(fullPrefix() +'1.2.0')
        commitNonPrimaryDir('6')

        when:
        VersionContext version = resolver.resolveVersion(versionRules, tagRules, nextVersionRules)

        then:
        version.previousVersion.toString() == '1.2.0'
        version.version.toString() == '1.2.0'
        !version.snapshot
    }

}
