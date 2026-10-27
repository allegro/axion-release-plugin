package pl.allegro.tech.build.extension

import com.github.zafarkhaja.semver.Version
import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.newInstance
import pl.allegro.tech.build.VersionInfo

abstract class TaglineExtension @Inject constructor(objects: ObjectFactory) {
    abstract val backend: Property<Backend>
    abstract val initialVersion: Property<String>
    abstract val versionInfo: Property<VersionInfo>

    val currentVersion: String get() = versionInfo.get().currentVersion
    val nextVersion: Version get() = versionInfo.get().nextVersion

    val versioning: VersioningSpec = objects.newInstance<VersioningSpec>()
    val releasing: ReleasingSpec = objects.newInstance<ReleasingSpec>()
    val git: GitSpec = objects.newInstance<GitSpec>()
    val gitlab: GitlabSpec = objects.newInstance<GitlabSpec>()
    val github: GithubSpec = objects.newInstance<GithubSpec>()

    init {
        backend.convention(Backend.AUTO)
        initialVersion.convention("0.1.0")

        versioning.branches.all.applyDefaults()
        versioning.branches.default.apply {
            versionCreator.convention(VersionCreatorType.VERSION)
            snapshot.enabled.convention(false)
            release.convention(true)
        }

        releasing.annotation.message.convention(
            AnnotationMessage { _, version, _ -> "Released version $version" }
        )

        git.signing.convention(false)
        git.requireAheadOfRemote.convention(true)
    }

    fun versioning(action: Action<in VersioningSpec>) = action.execute(versioning)

    fun releasing(action: Action<in ReleasingSpec>) = action.execute(releasing)

    fun git(action: Action<in GitSpec>) = action.execute(git)

    fun gitlab(action: Action<in GitlabSpec>) = action.execute(gitlab)

    fun github(action: Action<in GithubSpec>) = action.execute(github)
}
