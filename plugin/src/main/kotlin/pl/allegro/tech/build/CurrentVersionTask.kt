package pl.allegro.tech.build

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Prints the resolved project version")
abstract class CurrentVersionTask : DefaultTask() {
    @get:Input
    abstract val currentVersion: Property<String>

    @TaskAction
    fun printVersion() {
        logger.quiet(currentVersion.get())
    }
}
