package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.assertTrue

class SnapshotDependenciesCheckerTest  {

    SnapshotDependenciesChecker checker
    Project project

    @Before
    void setup(){
        ProjectBuilder builder = ProjectBuilder.builder()
        project= builder.build()
        project.configurations { implementation }
        checker = new SnapshotDependenciesChecker();
    }

    @Test
    void shouldDetectSnapshotVersions() {

        project.dependencies {
            implementation group: 'cicd', name:'sample', version: '2.0.3-SNAPSHOT'
        }
        Collection<String> snapshotVersions = checker.snapshotVersions(project)
        assertTrue("SNAPSHOT dependency entry not detected",!snapshotVersions.isEmpty())
    }

    @Test
    void shouldDetectConstraintsSnapshotVersions() {

        project.dependencies {
            constraints{
                implementation group: 'cicd', name:'sample', version: '2.0.3-SNAPSHOT'
            }
        }
        Collection<String> snapshotVersions = checker.snapshotVersions(project)
        assertTrue("SNAPSHOT dependency entry not detected",!snapshotVersions.isEmpty())
    }
}
