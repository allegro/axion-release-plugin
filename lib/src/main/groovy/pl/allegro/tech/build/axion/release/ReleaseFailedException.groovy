package pl.allegro.tech.build.axion.release

class ReleaseFailedException extends RuntimeException {

    ReleaseFailedException(String message) {
        super(message)
    }

    @Override
    Throwable fillInStackTrace() {
    }
}
