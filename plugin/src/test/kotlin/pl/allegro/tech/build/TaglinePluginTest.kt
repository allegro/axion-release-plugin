package pl.allegro.tech.build

import com.sun.net.httpserver.HttpServer
import java.io.File
import java.net.InetSocketAddress
import java.nio.file.Files
import java.util.Properties
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.eclipse.jgit.api.Git
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import pl.allegro.tech.build.extension.Backend
import pl.allegro.tech.build.extension.TagLookup

class TaglinePluginTest {
    private val revision = "a".repeat(40)

    @Test
    fun `GitHub tag pushes and published releases version real publications without Git or API credentials`() {
        val project = example("single-module")
        File(project, "settings.gradle.kts").appendText(
            """

                tagline { versioning { branches {
                    all { tagLookup = pl.allegro.tech.build.extension.TagLookup.HIGHEST_REACHABLE }
                    named("*") { tagPrefix = "branch-v" }
                } } }
            """.trimIndent(),
        )
        try {
            val events = listOf("push", "release")
            for (event in events) {
                val payload = if (event == "release") {
                    """{"action":"published","release":{"tag_name":"v1.2.3"},"repository":{"default_branch":"main"}}"""
                } else "{}"
                val environment = github(project, event, "v1.2.3", payload) + ("GITHUB_REF_TYPE" to "tag")
                val result = runner(project, environment, "currentVersion", "versionDetails", "generatePomFileForLibraryPublication", "--offline").build()
                assertTrue(result.output.lineSequence().any { it.trim() == "1.2.3" }, result.output)
                val details = details(project)
                assertEquals("1.2.3", details.getProperty("currentVersion"))
                assertEquals("1.2.4", details.getProperty("nextVersion"))
                assertEquals("v1.2.3", details.getProperty("releaseTag"))
                assertEquals("false", details.getProperty("snapshot"))
                assertPublication(project, "1.2.3")
                assertFalse(File(project, ".git").exists())
                if (event == "release") {
                    File(project, "event.json").writeText(payload.replace("v1.2.3", "v1.2.4"))
                    val changed = runner(project, environment, "currentVersion", "versionDetails",
                        "generatePomFileForLibraryPublication", "--offline").build()
                    assertFalse(changed.output.contains("Reusing configuration cache"), changed.output)
                    assertEquals("1.2.4", details(project).getProperty("currentVersion"))
                    assertPublication(project, "1.2.4")
                }
            }
        } finally {
            project.deleteRecursively()
        }
    }

    @Test
    fun `paginated GitHub branch and manual builds share versions across projects publications and cached builds`() {
        val project = example("multi-module")
        val requests = CopyOnWriteArrayList<String>()
        val publishedTag = AtomicReference<String?>()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/repos/example/library/tags") { exchange ->
            requests.add(exchange.requestURI.toString())
            val secondPage = exchange.requestURI.rawQuery.contains("page=2")
            val body = if (secondPage) {
                val added = publishedTag.get()?.let { ",{\"name\":\"$it\",\"commit\":{\"sha\":\"${"b".repeat(40)}\"}}" } ?: ""
                """[{"name":"v1.2.3","commit":{"sha":"$revision"}},{"name":"v9.0.0","commit":{"sha":"older"}}$added]"""
            } else {
                exchange.responseHeaders.add("Link", "</repos/example/library/tags?per_page=100&page=2>; rel=\"next\"")
                """[{"name":"v1.9.0","commit":{"sha":"older"}},{"name":"unrelated","commit":{"sha":"$revision"}}]"""
            }
            assertEquals("Bearer test-token", exchange.requestHeaders.getFirst("Authorization"))
            exchange.responseHeaders.add("Content-Type", "application/json")
            val bytes = body.toByteArray()
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        try {
            val environment = github(project, "push", "feature/foo") + mapOf(
                "GITHUB_TOKEN" to "test-token",
                "GITHUB_API_URL" to "http://127.0.0.1:${server.address.port}",
            )
            val tasks = arrayOf("currentVersion", "versionDetails", ":library:versionDetails", ":library:generatePomFileForLibraryPublication")
            runner(project, environment, *tasks).build()
            assertEquals(2, requests.size, "One paginated lookup should serve all projects")
            assertEquals("1.2.3", details(project).getProperty("currentVersion"), "An exact tag takes precedence over a higher unrelated tag")
            assertEquals("1.2.3", details(File(project, "library")).getProperty("projectVersion"))
            assertPublication(File(project, "library"), "1.2.3")

            val cached = runner(project, environment, *tasks).build()
            assertTrue(cached.output.contains("Reusing configuration cache"), cached.output)
            assertEquals(4, requests.size, "Cache validation rechecks external tags once per build")

            val branch = environment + ("GITHUB_SHA" to "b".repeat(40))
            runner(project, branch, *tasks).build()
            assertEquals("9.0.1-feature-foo-SNAPSHOT", details(project).getProperty("currentVersion"))
            assertEquals("9.0.0", details(project).getProperty("baseVersion"))
            assertEquals("", details(project).getProperty("releaseTag"))
            assertPublication(File(project, "library"), "9.0.1-feature-foo-SNAPSHOT")

            val manual = branch + mapOf("GITHUB_EVENT_NAME" to "workflow_dispatch", "GITHUB_REF_NAME" to "main")
            runner(project, manual, *tasks).build()
            assertEquals("9.0.1", details(project).getProperty("currentVersion"))
            assertEquals("false", details(project).getProperty("snapshot"))
            assertPublication(File(project, "library"), "9.0.1")
            assertEquals(8, requests.size)

            publishedTag.set("v9.0.1")
            val newlyTagged = runner(project, manual, *tasks).build()
            assertFalse(newlyTagged.output.contains("Reusing configuration cache"), newlyTagged.output)
            assertEquals("9.0.1", details(project).getProperty("currentVersion"))
            assertPublication(File(project, "library"), "9.0.1")
            assertEquals(12, requests.size, "Changed API input is read during cache validation and again during reconfiguration")
            assertFalse(File(project, ".git").exists())
        } finally {
            server.stop(0)
            project.deleteRecursively()
        }
    }

    @Test
    fun `local annotated tags and new commits invalidate cached versions for both local backends`() {
        for (backend in listOf("GIT", "JGIT")) {
            val project = example("single-module")
            File(project, "settings.gradle.kts").appendText("\ntagline { backend = pl.allegro.tech.build.extension.Backend.$backend }\n")
            try {
                Git.init().setDirectory(project).setInitialBranch("main").call().use { git ->
                    commit(git)
                    runner(project, emptyMap(), "versionDetails", "-Ptagline.initialVersion=0.3.0").build()
                    assertEquals("0.3.0", details(project).getProperty("currentVersion"))
                    assertEquals("false", details(project).getProperty("snapshot"))
                    assertEquals("0.3.0", details(project).getProperty("nextVersion"))
                    git.tag().setName("v1.2.3").setAnnotated(true).setSigned(false).setMessage("Release 1.2.3")
                        .setTagger(org.eclipse.jgit.lib.PersonIdent("Test", "test@example.com")).call()
                    val first = runner(project, emptyMap(), "currentVersion", "versionDetails").build()
                    assertEquals("1.2.3", details(project).getProperty("currentVersion"), first.output)
                    val cached = runner(project, emptyMap(), "currentVersion", "versionDetails").build()
                    assertTrue(cached.output.contains("Reusing configuration cache"), cached.output)
                    commit(git)
                    val changed = runner(project, emptyMap(), "currentVersion", "versionDetails").build()
                    assertEquals("1.2.4", details(project).getProperty("currentVersion"), changed.output)
                    assertEquals("false", details(project).getProperty("snapshot"))
                    assertFalse(changed.output.contains("Reusing configuration cache"), changed.output)
                    git.checkout().setName(git.repository.resolve("HEAD").name).call()
                    File(project, "settings.gradle.kts").appendText(
                        "\ntagline { versioning { branches { named(\"*\") { tagPrefix = \"branch-v\" } } } }\n",
                    )
                    val detached = runner(project, emptyMap(), "versionDetails").build()
                    assertEquals("1.2.4-SNAPSHOT", details(project).getProperty("currentVersion"), detached.output)
                }
            } finally {
                project.deleteRecursively()
            }
        }
    }

    @Test
    fun `CI resolution falls back to Git CLI and then JGit when provider access is unavailable`() {
        val project = example("single-module")
        val settings = File(project, "settings.gradle.kts")
        val original = settings.readText()
        val emptyPath = File(project, "empty-path").apply { mkdirs() }
        val requests = CopyOnWriteArrayList<String>()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/repos/example/library/tags") { exchange ->
            requests.add(exchange.requestURI.toString())
            exchange.sendResponseHeaders(503, -1)
            exchange.close()
        }
        server.start()
        try {
            val head = Git.init().setDirectory(project).setInitialBranch("main").call().use { git ->
                commit(git)
                git.tag().setName("v99.0.0").setAnnotated(false).setSigned(false).call()
                git.repository.resolve("HEAD").name
            }
            val environment = github(project, "push", "main") + ("GITHUB_SHA" to head)
            for (path in listOf(System.getenv("PATH"), emptyPath.absolutePath)) {
                val pathEnvironment = mapOf("PATH" to path)
                fun assertVersion(values: Map<String, String>, vararg options: String): BuildResult {
                    val result = runner(project, values + pathEnvironment, "currentVersion", "versionDetails", *options).build()
                    assertEquals("99.0.0", details(project).getProperty("currentVersion"), result.output)
                    return result
                }
                settings.writeText(original)
                assertVersion(mapOf("CI" to "true", "JENKINS_URL" to "https://jenkins.example"))
                assertVersion(environment)
                val unsupported = environment + ("GITHUB_EVENT_NAME" to "pull_request")
                val fallback = assertVersion(unsupported, "--info")
                assertTrue(fallback.output.contains("Tagline is using local Git: GitHub event 'pull_request' is not supported"), fallback.output)
                val cachedFallback = assertVersion(unsupported, "--info")
                assertTrue(cachedFallback.output.contains("Reusing configuration cache"), cachedFallback.output)

                for (strategy in listOf(TagLookup.NEAREST_REACHABLE, TagLookup.HIGHEST_REACHABLE)) {
                    settings.writeText(original +
                        "\ntagline { versioning { branches { all { tagLookup = pl.allegro.tech.build.extension.TagLookup.$strategy } } } }\n")
                    assertVersion(environment)
                }
                settings.writeText(original)
                val apiEnvironment = environment + mapOf(
                    "GITHUB_TOKEN" to "test-token", "GITHUB_API_URL" to "http://127.0.0.1:${server.address.port}",
                )
                assertVersion(apiEnvironment)
                val beforeOffline = requests.size
                assertVersion(apiEnvironment, "--offline")
                assertEquals(beforeOffline, requests.size, "Offline fallback must not query the provider API")
            }
            assertEquals(2, requests.size, "Each local fallback backend should handle one failed provider lookup")
        } finally {
            server.stop(0)
            project.deleteRecursively()
        }
    }

    @Test
    fun `explicit local backends retain CI branch identity on detached checkouts`() {
        val project = example("single-module")
        val settings = File(project, "settings.gradle.kts")
        val original = settings.readText()
        val requests = CopyOnWriteArrayList<String>()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            requests.add(exchange.requestURI.toString())
            exchange.sendResponseHeaders(503, -1)
            exchange.close()
        }
        server.start()
        try {
            val head = Git.init().setDirectory(project).setInitialBranch("main").call().use { git ->
                commit(git)
                git.tag().setName("v1.2.3").setAnnotated(false).setSigned(false).call()
                commit(git)
                val revision = git.repository.resolve("HEAD").name
                git.checkout().setName(revision).call()
                revision
            }
            val apiUrl = "http://127.0.0.1:${server.address.port}"
            val environments = listOf(
                github(project, "push", "feature-ci") + mapOf(
                    "GITHUB_SHA" to head, "GITHUB_TOKEN" to "test-token", "GITHUB_API_URL" to apiUrl,
                ),
                mapOf(
                    "GITLAB_CI" to "true", "CI_PIPELINE_SOURCE" to "push", "CI_COMMIT_REF_NAME" to "feature-ci",
                    "CI_COMMIT_SHA" to head, "CI_DEFAULT_BRANCH" to "main", "CI_PROJECT_ID" to "42",
                    "CI_JOB_TOKEN" to "test-token", "CI_API_V4_URL" to apiUrl,
                ),
            )
            for (backend in listOf("GIT", "JGIT")) {
                settings.writeText(original + "\ntagline { backend = pl.allegro.tech.build.extension.Backend.$backend }\n")
                for (environment in environments) {
                    val result = runner(project, environment, "currentVersion", "versionDetails", "generatePomFileForLibraryPublication").build()
                    val version = details(project)
                    assertEquals("1.2.4-feature-ci-SNAPSHOT", version.getProperty("currentVersion"), result.output)
                    assertEquals("feature-ci", version.getProperty("branch"))
                    assertEquals(head, version.getProperty("revision"))
                    assertPublication(project, "1.2.4-feature-ci-SNAPSHOT")
                    val cached = runner(project, environment, "currentVersion", "versionDetails", "generatePomFileForLibraryPublication").build()
                    assertTrue(cached.output.contains("Reusing configuration cache"), cached.output)
                }
            }
            assertTrue(requests.isEmpty(), "Explicit local backends must not query CI APIs: $requests")
        } finally {
            server.stop(0)
            project.deleteRecursively()
        }
    }

    @Test
    fun `local lookup strategies distinguish nearest reachable highest reachable and global tags`() {
        for (backend in listOf("GIT", "JGIT")) {
            val project = example("single-module")
            val settings = File(project, "settings.gradle.kts")
            val original = settings.readText()
            try {
                Git.init().setDirectory(project).setInitialBranch("main").call().use { git ->
                    commit(git)
                    git.tag().setName("v2.0.0").setAnnotated(true).setSigned(false).setMessage("Older high version")
                        .setTagger(org.eclipse.jgit.lib.PersonIdent("Test", "test@example.com")).call()
                    commit(git)
                    git.tag().setName("v1.0.0").setAnnotated(false).setSigned(false).call()
                    commit(git)
                    git.checkout().setCreateBranch(true).setName("other").call()
                    commit(git)
                    git.tag().setName("v99.0.0").setAnnotated(false).setSigned(false).call()
                    git.checkout().setName("main").call()

                    fun select(strategy: TagLookup) {
                        settings.writeText(original + """

                            tagline {
                                backend = pl.allegro.tech.build.extension.Backend.$backend
                                versioning { branches { all {
                                    tagLookup = pl.allegro.tech.build.extension.TagLookup.$strategy
                                } } }
                            }
                        """.trimIndent())
                    }

                    val expected = mapOf(
                        TagLookup.NEAREST_REACHABLE to "1.0.1",
                        TagLookup.HIGHEST_REACHABLE to "2.0.1",
                        TagLookup.HIGHEST_GLOBAL to "99.0.1",
                    )
                    for ((strategy, version) in expected) {
                        select(strategy)
                        val result = runner(project, emptyMap(), "currentVersion", "versionDetails").build()
                        assertEquals(version, details(project).getProperty("currentVersion"), "$backend $strategy: ${result.output}")
                    }

                    git.tag().setName("v0.5.0").setAnnotated(false).setSigned(false).call()
                    for (strategy in TagLookup.entries) {
                        select(strategy)
                        runner(project, emptyMap(), "currentVersion", "versionDetails").build()
                        assertEquals("0.5.0", details(project).getProperty("currentVersion"), "$backend $strategy must prefer an exact tag")
                    }
                }
            } finally {
                project.deleteRecursively()
            }
        }
    }

    private fun example(name: String): File {
        val source = File("../examples/$name")
        val destination = Files.createTempDirectory("tagline-$name-").toFile()
        source.walkTopDown().onEnter { it.name !in listOf(".gradle", "build", "gradle") }
            .filter { it.isFile && (it.extension == "kts" || it.name == "gradle.properties") }
            .forEach { file ->
                val target = File(destination, file.relativeTo(source).path)
                target.parentFile.mkdirs()
                target.writeText(file.readText().replace("includeBuild(\"../..\")", ""))
            }
        return destination
    }

    private fun github(
        project: File,
        event: String,
        ref: String,
        payload: String = """{"repository":{"default_branch":"main"}}""",
    ): Map<String, String> {
        val file = File(project, "event.json")
        file.writeText(payload)
        return mapOf(
            "GITHUB_ACTIONS" to "true", "GITHUB_EVENT_NAME" to event, "GITHUB_REF_TYPE" to "branch",
            "GITHUB_REF_NAME" to ref, "GITHUB_SHA" to revision, "GITHUB_EVENT_PATH" to file.absolutePath,
            "GITHUB_REPOSITORY" to "example/library",
        )
    }

    private fun runner(project: File, environment: Map<String, String>, vararg tasks: String): GradleRunner {
        val inherited = System.getenv().filterKeys { name ->
            name !in listOf("CI", "GH_TOKEN", "GLAB_TOKEN", "GITLAB_TOKEN") &&
                listOf("GITHUB_", "GITLAB_", "CI_", "JENKINS_").none(name::startsWith)
        }
        return GradleRunner.create().withPluginClasspath().withProjectDir(project)
            .withEnvironment(inherited + environment)
            .withArguments(*tasks, "--configuration-cache", "--configuration-cache-problems=fail", "--no-watch-fs", "--stacktrace",
                "-Dorg.gradle.java.installations.auto-download=false")
    }

    private fun details(project: File): Properties = Properties().apply {
        File(project, "build/version.properties").inputStream().use(::load)
    }

    private fun assertPublication(project: File, version: String) {
        val pom = File(project, "build/publications/library/pom-default.xml").readText()
        assertTrue(pom.contains("<version>$version</version>"), pom)
    }

    private fun commit(git: Git) {
        git.commit().setAllowEmpty(true).setSign(false).setMessage("Fixture commit")
            .setAuthor("Test", "test@example.com").setCommitter("Test", "test@example.com").call()
    }
}
