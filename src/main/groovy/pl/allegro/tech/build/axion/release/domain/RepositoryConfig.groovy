package pl.allegro.tech.build.axion.release.domain

class RepositoryConfig {

    String type = 'git'

    File directory

    String remote = 'origin'

    def customKey

    String customKeyPassword
    
    String customUsername
    
    String customPassword = ''

    boolean pushTagsOnly = false
}
