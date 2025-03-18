package pl.allegro.tech.build.axion.release.domain.scm;

public class ScmException extends RuntimeException {

    public ScmException(Throwable cause) {
        super(cause);
    }

    public ScmException(String message) {
        super(message);
    }
}
