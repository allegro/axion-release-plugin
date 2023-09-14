package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.TagPrefixConf
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties

import java.util.function.BiFunction
import java.util.regex.Matcher

import static java.lang.String.format

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
        if (!config.releaseBranchPattern) {
            config.releaseBranchPattern = context.isLegacyDefTagnameRepo() ? TagPrefixConf.DEFAULT_LEGACY_PREFIX + '/.+' : TagPrefixConf.defaultPrefix() + '/.+'
        }
        if (context.scmPosition.branch ==~ config.releaseBranchPattern) {
            return context.currentVersion.incrementPatchVersion()
        }
        return context.currentVersion.incrementMinorVersion()
    }),

    INCREMENT_PRERELEASE('incrementPrerelease', { VersionIncrementerContext context, Map config ->
        if (context.currentVersion.preReleaseVersion) {
            Matcher matcher = context.currentVersion.preReleaseVersion =~ /^(.*?)(\d+)$/
            if (matcher.matches()) {
                long nextNumber = Long.parseLong(matcher.group(2)) + 1
                String nextNumberPadded = format("%0" + matcher.group(2).length() + "d", nextNumber)
                String nextPreReleaseVersion = matcher.group(1) + nextNumberPadded

                return new Version.Builder()
                    .setNormalVersion(context.currentVersion.normalVersion)
                    .setPreReleaseVersion(nextPreReleaseVersion)
                    .build()
            }
        }

        if (config.initialPreReleaseIfNotOnPrerelease != null) {
            return context.currentVersion.incrementPatchVersion(String.valueOf(config.initialPreReleaseIfNotOnPrerelease))
        }

        return context.currentVersion.incrementPatchVersion()
    }),

    BRANCH_SPECIFIC('branchSpecific', { VersionIncrementerContext context, Map config ->
        def incrementer = config.find { context.scmPosition.branch ==~ it.key }
        return versionIncrementerFor(incrementer.value.toString(), config).apply(context)
    })

    private final String name

    final VersionIncrementer versionIncrementer

    private PredefinedVersionIncrementer(String name, Closure<Version> c) {
        this.name = name
        this.versionIncrementer = c
    }

    static VersionProperties.Incrementer versionIncrementerFor(String name, Map configuration = [:]) {
        PredefinedVersionIncrementer creator = values().find { it.name == name }
        if (creator == null) {
            throw new IllegalArgumentException("There is no predefined version incrementer with $name name. " +
                "You can choose from: ${values().collect { it.name }}")
        }
        return { VersionIncrementerContext context -> creator.versionIncrementer.apply(context, configuration) }
    }

    interface VersionIncrementer extends BiFunction<VersionIncrementerContext,Map,Version> {}
}
