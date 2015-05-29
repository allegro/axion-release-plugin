package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version

import java.util.regex.Matcher
import static String.format

enum PredefinedVersionIncrementer {

    INCREMENT_PATCH_VERSION('incrementPatch', { VersionIncrementerContext context ->
        return context.version.incrementPatchVersion()
    }),

    INCREMENT_MINOR_VERSION('incrementMinor', { VersionIncrementerContext context ->
        return context.version.incrementMinorVersion()
    }),

    INCREMENT_MINOR_IF_NOT_ON_RELEASE_BRANCH('incrementMinorIfNotOnRelease', { VersionIncrementerContext context ->
        if (context.versionConfig.releaseBranchPattern.matcher(context.scmPosition.branch).matches()) {
            return context.version.incrementPatchVersion()
        }
        return context.version.incrementMinorVersion()
    }),

    INCREMENT_PRERELEASE('incrementPrerelease', { VersionIncrementerContext context ->
        // version.incrementPreReleaseVersion() does increment -rc1 into -rc1.1, so incrementing manually
        if (context.version.preReleaseVersion) {
            Matcher matcher = context.version.preReleaseVersion =~ /^(.*?)(\d+)$/
            if (matcher.matches()) {
                long nextNumber = Long.parseLong(matcher.group(2)) + 1
                String nextNumberPadded = format("%0" + matcher.group(2).length() + "d", nextNumber);
                String nextPreReleaseVersion = matcher.group(1) + nextNumberPadded

                return new Version.Builder()
                        .setNormalVersion(context.version.normalVersion)
                        .setPreReleaseVersion(nextPreReleaseVersion)
                        .build();
            }
        }
        return context.version.incrementPatchVersion()
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
