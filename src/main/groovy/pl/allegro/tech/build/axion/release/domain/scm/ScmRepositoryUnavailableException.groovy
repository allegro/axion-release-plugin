package pl.allegro.tech.build.axion.release.domain.scm

class ScmRepositoryUnavailableException extends RuntimeException {

    ScmRepositoryUnavailableException(Throwable cause) {
        super(cause)
    }

    ScmRepositoryUnavailableException(String message) {
        super(message)
    }

}

