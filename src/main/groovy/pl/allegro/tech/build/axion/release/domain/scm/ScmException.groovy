package pl.allegro.tech.build.axion.release.domain.scm

class ScmException extends RuntimeException {

    ScmException(Throwable cause) {
        super(cause)
    }

    ScmException(String message) {
        super(message)
    }
}
