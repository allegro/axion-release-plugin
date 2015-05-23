package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

import java.util.regex.Matcher
import static String.format

enum PredefinedVersionIncrementer {

    INCREMENT_PATCH_VERSION('incrementPatchVersion', { Version version, ScmPosition position, VersionConfig config ->
        return version.incrementPatchVersion()
    }),

    INCREMENT_MINOR_VERSION('incrementMinorVersion', { Version version, ScmPosition position, VersionConfig config ->
        return version.incrementMinorVersion()
    }),

    INCREMENT_MINOR_IF_NOT_ON_RELEASE_BRANCH('incrementMinorIfNotOnRelease', { Version version, ScmPosition position,
                                                                               VersionConfig config ->
        if (config.releaseBranchPattern.matcher(position.branch).matches()) {
            return version.incrementPatchVersion()
        }
        return version.incrementMinorVersion()
    }),

    INCREMENT_PRERELEASE('incrementPrerelease', { Version version, ScmPosition position, VersionConfig config ->
        // version.incrementPreReleaseVersion() does increment -rc1 into -rc1.1, so incrementing manually
        if (version.preReleaseVersion) {
            Matcher matcher = version.preReleaseVersion =~ /^(.*?)(\d+)$/
            if (matcher.matches()) {
                long nextNumber = Long.parseLong(matcher.group(2)) + 1
                String nextNumberPadded = format("%0" + matcher.group(2).length() + "d", nextNumber);
                String nextPreReleaseVersion = matcher.group(1) + nextNumberPadded

                return new Version.Builder()
                        .setNormalVersion(version.normalVersion)
                        .setPreReleaseVersion(nextPreReleaseVersion)
                        .build();
            }
        }
        return version.incrementPatchVersion()
    })

    private final String name

    final Closure<Version> versionIncrementer

    private PredefinedVersionIncrementer(String name, Closure<Version> c) {
        this.name = name
        this.versionIncrementer = c
    }

    static Closure<Version> versionIncrementerFor(String name) {
        PredefinedVersionIncrementer creator = values().find { it.name == name }
        if (creator == null) {
            throw new IllegalArgumentException("There is no predefined version incrementer with $name name. " +
                    "You can choose from: ${values().collect { it.name }}");
        }
        return creator.versionIncrementer
    }
}
