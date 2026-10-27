package pl.allegro.tech.build.repository

import java.io.File
import pl.allegro.tech.build.execution.ExecutionContext
import pl.allegro.tech.build.execution.local.JGitEnvironment
import pl.allegro.tech.build.execution.local.LocalGitEnvironment
import pl.allegro.tech.build.execution.local.LocalRepository
import pl.allegro.tech.build.extension.Backend

internal object Adapters {
    fun metadataReader(backend: Backend, context: ExecutionContext, directory: File): RepositoryMetadataReader = when (context) {
        is ExecutionContext.Ci -> RepositoryMetadataReader { context.metadata }
        is ExecutionContext.Local -> RepositoryMetadataReader {
            context.fallbackFailure?.let(LocalFallback::report)
            LocalFallback.preservingFailure(context.fallbackFailure) {
                localRepository(backend, context, directory).readMetadata()
            }
        }
    }

    fun tagReader(backend: Backend, context: ExecutionContext, directory: File, offline: Boolean): TagReader = when (context) {
        is ExecutionContext.Ci -> if (backend == Backend.AUTO) {
            FallbackTagReader(
                TagReader { prefix, tagLookup -> context.resolveTags(prefix, tagLookup, offline) },
                { localRepository(context.fallback.executablePath, directory) },
            )
        } else {
            localRepository(backend, context.fallback, directory)
        }
        is ExecutionContext.Local -> TagReader { prefix, tagLookup ->
            LocalFallback.preservingFailure(context.fallbackFailure) {
                localRepository(backend, context, directory).resolveTags(prefix, tagLookup)
            }
        }
    }

    private fun localRepository(backend: Backend, context: ExecutionContext.Local, directory: File): LocalRepository = when (backend) {
        Backend.AUTO, Backend.GIT -> localRepository(context.executablePath, directory)
        Backend.JGIT -> JGitEnvironment(directory)
    }

    private fun localRepository(path: String?, directory: File): LocalRepository =
        if (LocalGitEnvironment.isAvailable(path)) LocalGitEnvironment(directory) else JGitEnvironment(directory)
}
