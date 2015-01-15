package pl.allegro.tech.build.axion.release.domain

class RepositoryConfig {

    String type = 'git'

    File directory

    String remote = 'origin'

    String customKey

    String customKeyPassword
}
