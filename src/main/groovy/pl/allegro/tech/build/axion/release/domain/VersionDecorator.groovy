package pl.allegro.tech.build.axion.release.domain

import java.util.regex.Pattern

class VersionDecorator {

    String createVersion(VersionConfig config, VersionWithPosition rawVersion) {
         Closure versionCreator = config.versionCreator

        if (!rawVersion.forcedVersion() && config.branchVersionCreators != null) {
            String branchName = rawVersion.position.branch
            for (Map.Entry<Pattern, Closure> entry : config.branchVersionCreators.entrySet()) {
                if (Pattern.matches(entry.key, branchName)) {
                    versionCreator = entry.value
                    break
                }
            }
        }

        return versionCreator(rawVersion.version.toString(), rawVersion.position)
    }

}
