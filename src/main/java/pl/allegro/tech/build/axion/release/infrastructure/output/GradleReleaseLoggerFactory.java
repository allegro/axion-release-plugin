package pl.allegro.tech.build.axion.release.infrastructure.output;

import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.slf4j.Marker;
import pl.allegro.tech.build.axion.release.domain.logging.ReleaseLogger;
import pl.allegro.tech.build.axion.release.domain.logging.ReleaseLoggerFactory;

public class GradleReleaseLoggerFactory implements ReleaseLoggerFactory {
    @Override
    public ReleaseLogger logger(Class<?> clazz) {
        return new GradleReleaseLogger(Logging.getLogger(clazz));
    }

    @Override
    public ReleaseLogger logger(String name) {
        return new GradleReleaseLogger(Logging.getLogger(name));
    }

    private static class GradleReleaseLogger implements ReleaseLogger, Logger {
        private final Logger logger;

        public GradleReleaseLogger(Logger logger) {
            this.logger = logger;
        }

        @Override
        public boolean isLifecycleEnabled() {
            return logger.isLifecycleEnabled();
        }

        @Override
        public void debug(String s, Object... objects) {
            logger.debug(s, objects);
        }

        @Override
        public void lifecycle(String s) {
            logger.lifecycle(s);
        }

        @Override
        public void lifecycle(String s, Object... objects) {
            logger.lifecycle(s, objects);
        }

        @Override
        public void lifecycle(String s, Throwable throwable) {
            logger.lifecycle(s, throwable);
        }

        @Override
        public boolean isQuietEnabled() {
            return logger.isQuietEnabled();
        }

        @Override
        public void quiet(String s) {
            logger.quiet(s);
        }

        @Override
        public void quiet(String s, Object... objects) {
            logger.quiet(s, objects);
        }

        @Override
        public void info(String s, Object... objects) {
            logger.info(s, objects);
        }

        @Override
        public void quiet(String s, Throwable throwable) {
            logger.quiet(s, throwable);
        }

        @Override
        public boolean isEnabled(LogLevel logLevel) {
            return logger.isEnabled(logLevel);
        }

        @Override
        public void log(LogLevel logLevel, String s) {
            logger.log(logLevel, s);
        }

        @Override
        public void log(LogLevel logLevel, String s, Object... objects) {
            logger.log(logLevel, s, objects);
        }

        @Override
        public void log(LogLevel logLevel, String s, Throwable throwable) {
            logger.log(logLevel, s, throwable);
        }

        @Override
        public String getName() {
            return logger.getName();
        }

        @Override
        public boolean isTraceEnabled() {
            return logger.isTraceEnabled();
        }

        @Override
        public void trace(String s) {
            logger.trace(s);
        }

        @Override
        public void trace(String s, Object o) {
            logger.trace(s, o);
        }

        @Override
        public void trace(String s, Object o, Object o1) {
            logger.trace(s, o, o1);
        }

        @Override
        public void trace(String s, Object... objects) {
            logger.trace(s, objects);
        }

        @Override
        public void trace(String s, Throwable throwable) {
            logger.trace(s, throwable);
        }

        @Override
        public boolean isTraceEnabled(Marker marker) {
            return logger.isTraceEnabled(marker);
        }

        @Override
        public void trace(Marker marker, String s) {
            logger.trace(marker, s);
        }

        @Override
        public void trace(Marker marker, String s, Object o) {
            logger.trace(marker, s, o);
        }

        @Override
        public void trace(Marker marker, String s, Object o, Object o1) {
            logger.trace(marker, s, o, o1);
        }

        @Override
        public void trace(Marker marker, String s, Object... objects) {
            logger.trace(marker, s, objects);
        }

        @Override
        public void trace(Marker marker, String s, Throwable throwable) {
            logger.trace(marker, s, throwable);
        }

        @Override
        public boolean isDebugEnabled() {
            return logger.isDebugEnabled();
        }

        @Override
        public void debug(String s) {
            logger.debug(s);
        }

        @Override
        public void debug(String s, Object o) {
            logger.debug(s, o);
        }

        @Override
        public void debug(String s, Object o, Object o1) {
            logger.debug(s, o, o1);
        }

        @Override
        public void debug(String s, Throwable throwable) {
            logger.debug(s, throwable);
        }

        @Override
        public boolean isDebugEnabled(Marker marker) {
            return logger.isDebugEnabled(marker);
        }

        @Override
        public void debug(Marker marker, String s) {
            logger.debug(marker, s);
        }

        @Override
        public void debug(Marker marker, String s, Object o) {
            logger.debug(marker, s, o);
        }

        @Override
        public void debug(Marker marker, String s, Object o, Object o1) {
            logger.debug(marker, s, o, o1);
        }

        @Override
        public void debug(Marker marker, String s, Object... objects) {
            logger.debug(marker, s, objects);
        }

        @Override
        public void debug(Marker marker, String s, Throwable throwable) {
            logger.debug(marker, s, throwable);
        }

        @Override
        public boolean isInfoEnabled() {
            return logger.isInfoEnabled();
        }

        @Override
        public void info(String s) {
            logger.info(s);
        }

        @Override
        public void info(String s, Object o) {
            logger.info(s, o);
        }

        @Override
        public void info(String s, Object o, Object o1) {
            logger.info(s, o, o1);
        }

        @Override
        public void info(String s, Throwable throwable) {
            logger.info(s, throwable);
        }

        @Override
        public boolean isInfoEnabled(Marker marker) {
            return logger.isInfoEnabled(marker);
        }

        @Override
        public void info(Marker marker, String s) {
            logger.info(marker, s);
        }

        @Override
        public void info(Marker marker, String s, Object o) {
            logger.info(marker, s, o);
        }

        @Override
        public void info(Marker marker, String s, Object o, Object o1) {
            logger.info(marker, s, o, o1);
        }

        @Override
        public void info(Marker marker, String s, Object... objects) {
            logger.info(marker, s, objects);
        }

        @Override
        public void info(Marker marker, String s, Throwable throwable) {
            logger.info(marker, s, throwable);
        }

        @Override
        public boolean isWarnEnabled() {
            return logger.isWarnEnabled();
        }

        @Override
        public void warn(String s) {
            logger.warn(s);
        }

        @Override
        public void warn(String s, Object o) {
            logger.warn(s, o);
        }

        @Override
        public void warn(String s, Object... objects) {
            logger.warn(s, objects);
        }

        @Override
        public void warn(String s, Object o, Object o1) {
            logger.warn(s, o, o1);
        }

        @Override
        public void warn(String s, Throwable throwable) {
            logger.warn(s, throwable);
        }

        @Override
        public boolean isWarnEnabled(Marker marker) {
            return logger.isWarnEnabled(marker);
        }

        @Override
        public void warn(Marker marker, String s) {
            logger.warn(marker, s);
        }

        @Override
        public void warn(Marker marker, String s, Object o) {
            logger.warn(marker, s, o);
        }

        @Override
        public void warn(Marker marker, String s, Object o, Object o1) {
            logger.warn(marker, s, o, o1);
        }

        @Override
        public void warn(Marker marker, String s, Object... objects) {
            logger.warn(marker, s, objects);
        }

        @Override
        public void warn(Marker marker, String s, Throwable throwable) {
            logger.warn(marker, s, throwable);
        }

        @Override
        public boolean isErrorEnabled() {
            return logger.isErrorEnabled();
        }

        @Override
        public void error(String s) {
            logger.error(s);
        }

        @Override
        public void error(String s, Object o) {
            logger.error(s, o);
        }

        @Override
        public void error(String s, Object o, Object o1) {
            logger.error(s, o, o1);
        }

        @Override
        public void error(String s, Object... objects) {
            logger.error(s, objects);
        }

        @Override
        public void error(String s, Throwable throwable) {
            logger.error(s, throwable);
        }

        @Override
        public boolean isErrorEnabled(Marker marker) {
            return logger.isErrorEnabled(marker);
        }

        @Override
        public void error(Marker marker, String s) {
            logger.error(marker, s);
        }

        @Override
        public void error(Marker marker, String s, Object o) {
            logger.error(marker, s, o);
        }

        @Override
        public void error(Marker marker, String s, Object o, Object o1) {
            logger.error(marker, s, o, o1);
        }

        @Override
        public void error(Marker marker, String s, Object... objects) {
            logger.error(marker, s, objects);
        }

        @Override
        public void error(Marker marker, String s, Throwable throwable) {
            logger.error(marker, s, throwable);
        }
    }
}
