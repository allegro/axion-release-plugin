package pl.allegro.tech.build.axion.release.domain.hooks

class DefaultReleaseHookFactory implements ReleaseHookFactory {
    
    @Override
    ReleaseHookAction create() {
        throw new UnsupportedOperationException("${this.class} does not construct release hooks without arguments")
    }

    @Override
    ReleaseHookAction create(Map arguments) {
        throw new UnsupportedOperationException("${this.class} does not construct release hooks from Map")
    }

    @Override
    ReleaseHookAction create(Closure customAction) {
        throw new UnsupportedOperationException("${this.class} does not construct release hooks from Closure")
    }
}
