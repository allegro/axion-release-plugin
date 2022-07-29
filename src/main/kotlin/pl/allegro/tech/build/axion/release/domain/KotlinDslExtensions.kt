package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Action
import pl.allegro.tech.build.axion.release.domain.hooks.HookContext
import pl.allegro.tech.build.axion.release.domain.hooks.HooksConfig
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHookAction
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

/*
Examples of how Kotlin extension variables can be used; not implemented atm as varying config
objects have inconsistencies in exposing variables vs setters
 */
//var TagNameSerializationConfig.tagNameSerializer : (TagProperties,String) -> String
//    get() = { a, b -> serialize.apply(a,b) }
//    set(value) = serializer(value)
//
//var TagNameSerializationConfig.tagNameDeserializer : (TagProperties, ScmPosition, String) -> String
//    get() =  {a,b,c -> deserialize.apply(a,b,c) }
//    set(value) = deserializer(value)
//
//var NextVersionConfig.versionSerializer : (NextVersionProperties,String) -> String
//    get() = { a,b -> serializer.apply(a,b) }
//    set(value) { serializer = NextVersionProperties.Serializer(value) }
//
//var NextVersionConfig.versionDeserializer : (NextVersionProperties,ScmPosition,String) -> String
//    get() = { a,b,c -> deserializer.apply(a,b,c) }
//    set(value) { deserializer = NextVersionProperties.Deserializer(value) }

fun HooksConfig.preRelease(hookConfig: Action<ReleaseHooksBuilder>) {
    val preReleaseHooksBuilder = ReleaseHooksBuilder(this, true)
    hookConfig.execute(preReleaseHooksBuilder)
}

fun HooksConfig.postRelease(hookConfig: Action<ReleaseHooksBuilder>) {
    val postReleaseHooksBuilder = ReleaseHooksBuilder(this, false)
    hookConfig.execute(postReleaseHooksBuilder)
}

class ReleaseHooksBuilder(private val hooksConfig: HooksConfig, private val preRelease: Boolean) {
    fun fileUpdate(fileUpdateConfig: Action<FileUpdateSpec>) {
        val fileUpdateSpec = FileUpdateSpec()
        fileUpdateConfig.execute(fileUpdateSpec)
        val configMap = mapOf(
            "files" to fileUpdateSpec.filesToUpdate,
            "pattern" to fileUpdateSpec.pattern,
            "replacement" to fileUpdateSpec.replacement,
            "encoding" to fileUpdateSpec.encoding
        )
        if (preRelease) {
            hooksConfig.pre("fileUpdate", configMap)
        } else {
            hooksConfig.post("fileUpdate", configMap)
        }
    }

    fun commit(action: (String, ScmPosition) -> String) {
        val hook = ReleaseHookAction { c: HookContext -> action(c.releaseVersion, c.position) }
        if (preRelease) {
            hooksConfig.pre("commit", hook)
        } else {
            hooksConfig.post("commit", hook)
        }
    }

    fun push() {
        if (preRelease) {
            hooksConfig.pre("push")
        } else {
            hooksConfig.post("push")
        }
    }

    fun custom(action: (HookContext) -> Unit) {
        if (preRelease) {
            hooksConfig.pre(ReleaseHookAction(action))
        } else {
            hooksConfig.post(ReleaseHookAction(action))
        }
    }
}

class FileUpdateSpec {
    internal var filesToUpdate = mutableListOf<Any>()
    fun file(file: String) {
        filesToUpdate.add(file)
    }

    var pattern: (String, HookContext) -> String = { previousVersion, context -> "" }
    var replacement: (String, HookContext) -> String = { currentVersion, context -> "" }
    var encoding: String? = null
}
