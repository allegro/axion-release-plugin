package pl.allegro.tech.build.axion.release.domain.hooks

class DefaultReleaseHookFactory implements ReleaseHookFactory {
    
    @Override
    ReleaseHook create() {
        throw new UnsupportedOperationException("${this.class} does not construct release hooks without arguments")
    }

    @Override
    ReleaseHook create(Map arguments) {
        throw new UnsupportedOperationException("${this.class} does not construct release hooks from Map")
    }

    @Override
    ReleaseHook create(Closure customAction) {
        throw new UnsupportedOperationException("${this.class} does not construct release hooks from Closure")
    }
}
