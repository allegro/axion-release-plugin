package pl.allegro.tech.build.axion.release.domain.hooks;

import groovy.lang.Closure;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;

import java.util.Map;
import java.util.function.BiFunction;

public interface ReleaseHookFactory {

    ReleaseHookAction create();

    ReleaseHookAction create(Map<String, Object> arguments);

    ReleaseHookAction create(Closure customAction);
}
