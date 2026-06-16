package pl.allegro.tech.build.axion.release

import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions

internal fun git(vararg args: String, workingDir: File, config: RepositoryConfig) {
    var tempKeyFile: File? = null
    try {
        val sshEnv: Map<String, String> = when {
            config.customKey != null -> {
                val perms = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))
                tempKeyFile = Files.createTempFile("axion-key-", ".pem", perms).toFile()
                tempKeyFile!!.writeText(config.customKey!!)
                mapOf("GIT_SSH_COMMAND" to sshCommand(tempKeyFile!!.absolutePath, config.customKeyPassword))
            }
            config.customKeyFile != null ->
                mapOf("GIT_SSH_COMMAND" to sshCommand(config.customKeyFile!!.absolutePath, config.customKeyPassword))
            else -> emptyMap()
        }

        val exitCode = ProcessBuilder("git", *args)
            .directory(workingDir)
            .inheritIO()
            .also { pb -> pb.environment().putAll(sshEnv) }
            .start()
            .waitFor()
        check(exitCode == 0) { "git ${args.joinToString(" ")} failed with exit code $exitCode" }
    } finally {
        tempKeyFile?.delete()
    }
}

private fun sshCommand(keyPath: String, password: String?): String {
    val base = "ssh -i $keyPath -o StrictHostKeyChecking=accept-new -o BatchMode=yes"
    return if (password != null) "$base -o IdentityAgent=none" else base
}
