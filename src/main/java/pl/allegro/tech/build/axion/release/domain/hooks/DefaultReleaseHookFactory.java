package pl.allegro.tech.build.axion.release.domain.hooks;

import groovy.lang.Closure;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;

import java.util.Map;
import java.util.function.BiFunction;

public class DefaultReleaseHookFactory implements ReleaseHookFactory {

    @Override
    public ReleaseHookAction create() {
        throw new UnsupportedOperationException(this.getClass() + " does not construct release hooks without arguments");
    }

    @Override
    public ReleaseHookAction create(Map<String, Object> arguments) {
        throw new UnsupportedOperationException(this.getClass() + " does not construct release hooks from Map");
    }

    @Override
    public ReleaseHookAction create(Closure customAction) {
        throw new UnsupportedOperationException(this.getClass() + " does not construct release hooks from Closure");
    }

}
