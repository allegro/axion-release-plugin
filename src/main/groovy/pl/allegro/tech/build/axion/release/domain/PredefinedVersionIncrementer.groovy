package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version

import java.util.regex.Matcher
import static String.format

enum PredefinedVersionIncrementer {

    INCREMENT_PATCH('incrementPatch', { VersionIncrementerContext context, Map config ->
        return context.currentVersion.incrementPatchVersion()
    }),

    INCREMENT_MINOR('incrementMinor', { VersionIncrementerContext context, Map config ->
        return context.currentVersion.incrementMinorVersion()
    }),

    INCREMENT_MAJOR('incrementMajor', { VersionIncrementerContext context, Map config ->
        return context.currentVersion.incrementMajorVersion()
    }),

    INCREMENT_MINOR_IF_NOT_ON_RELEASE_BRANCH('incrementMinorIfNotOnRelease', { VersionIncrementerContext context, Map config ->
        if(!config.releaseBranchPattern) {
            config.releaseBranchPattern = 'release/.+'
        }
        if(context.scmPosition.branch ==~ config.releaseBranchPattern) {
            return context.currentVersion.incrementPatchVersion()
        }
        return context.currentVersion.incrementMinorVersion()
    }),

    INCREMENT_PRERELEASE('incrementPrerelease', { VersionIncrementerContext context, Map config ->
        if (context.currentVersion.preReleaseVersion) {
            Matcher matcher = context.currentVersion.preReleaseVersion =~ /^(.*?)(\d+)$/
            if (matcher.matches()) {
                long nextNumber = Long.parseLong(matcher.group(2)) + 1
                String nextNumberPadded = format("%0" + matcher.group(2).length() + "d", nextNumber);
                String nextPreReleaseVersion = matcher.group(1) + nextNumberPadded

                return new Version.Builder()
                        .setNormalVersion(context.currentVersion.normalVersion)
                        .setPreReleaseVersion(nextPreReleaseVersion)
                        .build();
            }
        }
        return context.currentVersion.incrementPatchVersion()
    }),

    CREATE_MAJOR_RC('createMajorRC', { VersionIncrementerContext context, Map config ->
        throwErrorIfPrerelease(context)
        return new Version.Builder()
            .setNormalVersion(context.currentVersion.incrementMajorVersion().toString())
            .setPreReleaseVersion("RC1")
            .build();
    }),

    CREATE_MINOR_RC('createMinorRC', { VersionIncrementerContext context, Map config ->
        throwErrorIfPrerelease(context)
        return new Version.Builder()
            .setNormalVersion(context.currentVersion.incrementMinorVersion().toString())
            .setPreReleaseVersion("RC1")
            .build();
    }),

    CREATE_FINAL('createFinal', { VersionIncrementerContext context, Map config ->
        throwErrorIfNotPrerelease(context)
        return new Version.Builder()
                .setNormalVersion(context.currentVersion.normalVersion)
                .build();
    }),

    BRANCH_SPECIFIC('branchSpecific', { VersionIncrementerContext context, Map config ->
        def incrementer = config.find { context.scmPosition.branch ==~ it.key }
        return versionIncrementerFor(incrementer.value, config)(context)
    })

    private final String name

    final Closure<Version> versionIncrementer

    private static throwErrorIfPrerelease(VersionIncrementerContext context) {
        if (context.currentVersion.getPreReleaseVersion()) {
            throw new IllegalArgumentException("Already on a prerelease, use incrementPrerelease")
        }
    }

    private static throwErrorIfNotPrerelease(VersionIncrementerContext context) {
        if (!context.currentVersion.getPreReleaseVersion()) {
            throw new IllegalArgumentException("Not on a prerelease, use normal release instead")
        }
    }

    private PredefinedVersionIncrementer(String name, Closure<Version> c) {
        this.name = name
        this.versionIncrementer = c
    }

    static Closure<Version> versionIncrementerFor(String name, Map configuration = [:]) {
        PredefinedVersionIncrementer creator = values().find { it.name == name }
        if (creator == null) {
            throw new IllegalArgumentException("There is no predefined version incrementer with $name name. " +
                    "You can choose from: ${values().collect { it.name }}");
        }
        return { VersionIncrementerContext context -> creator.versionIncrementer(context, configuration) }
    }
}
