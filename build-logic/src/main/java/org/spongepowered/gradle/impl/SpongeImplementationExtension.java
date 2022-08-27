package org.spongepowered.gradle.impl;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.SourceSet;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class SpongeImplementationExtension {

    public static final String MIXIN_CONFIGS_PROPERTY = "mixinConfigs";

    private final Project project;
    private final Logger logger;

    @Inject
    public SpongeImplementationExtension(final Project project, final Logger logger) {
        this.project = project;
        this.logger = logger;
    }

    public void applyNamedDependencyOnOutput(final Project originProject, final SourceSet sourceAdding, final SourceSet targetSource, final Project implProject, final String dependencyConfigName) {
        implProject.getLogger().lifecycle(
            "[{}] Adding {}({}) to {}({}).{}",
            implProject.getName(),
            originProject.getPath(),
            sourceAdding.getName(),
            implProject.getPath(),
            targetSource.getName(),
            dependencyConfigName
        );

        implProject.getDependencies().add(dependencyConfigName, sourceAdding.getOutput());
    }

    public String generateImplementationVersionString(final String apiVersion, final String minecraftVersion, final String implRecommendedVersion) {
        return this.generateImplementationVersionString(apiVersion, minecraftVersion, implRecommendedVersion, null);
    }

    public String generateImplementationVersionString(final String apiVersion, final String minecraftVersion, final String implRecommendedVersion, final String addedVersionInfo) {
        final String latestApiVersion = generateApiReleasedVersion(apiVersion);
        final String implementationVersion = latestApiVersion + "." + implRecommendedVersion;

        return Stream.of(minecraftVersion, addedVersionInfo, implementationVersion)
            .filter(Objects::nonNull)
            .collect(Collectors.joining("-"));
    }

    private static String generateApiReleasedVersion(final String apiVersion) {
        final String[] apiSplit = apiVersion.replace("-SNAPSHOT", "").split("\\.");
        final boolean isSnapshot = apiVersion.contains("-SNAPSHOT");

        // This is to determine if the split api version has at the least a minimum version.
        final String apiMajor = apiSplit[0];
        final String minorVersion;
        if (apiSplit.length > 1) {
            minorVersion = apiSplit[1];
        } else {
            minorVersion = "0";
        }
        final int latestReleasedVersion = Math.max(Integer.parseInt(minorVersion) - 1, 0);
        // And then here, we determine if the api version still has a patch version, to just ignore it.
        final String latestReleasedApiMinor = isSnapshot ? String.valueOf(latestReleasedVersion) : minorVersion;
        return apiMajor + "." + latestReleasedApiMinor;
    }

    public String generatePlatformBuildVersionString(final String apiVersion, final String minecraftVersion, final String implRecommendedVersion) {
        return this.generatePlatformBuildVersionString(apiVersion, minecraftVersion, implRecommendedVersion, null);
    }

    public String generatePlatformBuildVersionString(final String apiVersion, final String minecraftVersion, final String implRecommendedVersion, final String addedVersionInfo) {
        final boolean isRelease = !implRecommendedVersion.endsWith("-SNAPSHOT");

        this.logger.lifecycle("Detected Implementation Version {} as {}", implRecommendedVersion, isRelease ? "Release" : "Snapshot");
        final String apiReleaseVersion = generateApiReleasedVersion(apiVersion);
        final String rawBuildNumber = System.getenv("BUILD_NUMBER");
        final int buildNumber = Integer.parseInt(rawBuildNumber == null ? "0" : rawBuildNumber);
        final String implVersionAsReleaseCandidateOrRecommended;
        if (isRelease) {
            implVersionAsReleaseCandidateOrRecommended = apiReleaseVersion + '.' + implRecommendedVersion;
        } else {
            implVersionAsReleaseCandidateOrRecommended = apiReleaseVersion + '.' + implRecommendedVersion.replace("-SNAPSHOT", "") + "-RC" + buildNumber;
        }
        return Stream.of(minecraftVersion, addedVersionInfo, implVersionAsReleaseCandidateOrRecommended)
            .filter(Objects::nonNull)
            .collect(Collectors.joining("-"));
    }

    /**
     * Get all mixin configurations that should be applied to this project
     * @return the mixin configuration files that should be applied to this project
     */
    public Set<String> getMixinConfigurations() {
        return this.getNamedConfigurations(SpongeImplementationExtension.MIXIN_CONFIGS_PROPERTY);
    }

    public Set<String> getNamedConfigurations(final String name) {
        final Set<String> configs = new HashSet<>();

        // if we have a parent
        final Project parentProject = this.project.getParent();
        if (parentProject != null) {
            SpongeImplementationExtension
                .splitAndAddIfNonNull(configs, (String) parentProject.findProperty(name));
        }

        // own project
        SpongeImplementationExtension.splitAndAddIfNonNull(configs, (String) this.project.findProperty(name));
        return configs;
    }

    private static void splitAndAddIfNonNull(final Collection<String> collector, final @Nullable String property) {
        if (property == null) {
            return;
        }

        final String[] split = property.split(",");
        Collections.addAll(collector, split);
    }

}
