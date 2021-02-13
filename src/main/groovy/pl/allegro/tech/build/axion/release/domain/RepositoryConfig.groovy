package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional

class RepositoryConfig {

    @Input
    String type = 'git'

    @InputDirectory
    File directory

    @Input
    String remote = 'origin'

    @Input
    @Optional
    def customKey

    @Input
    @Optional
    String customKeyPassword

    @Input
    @Optional
    String customUsername

    @Input
    String customPassword = ''

    @Input
    boolean pushTagsOnly = false
}
