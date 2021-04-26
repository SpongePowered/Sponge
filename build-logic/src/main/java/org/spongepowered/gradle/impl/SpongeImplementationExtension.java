package org.spongepowered.gradle.impl;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.SourceSet;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

public abstract class SpongeImplementationExtension {

    private final Logger logger;

    @Inject
    public SpongeImplementationExtension(final Logger logger) {
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
        final String[] apiSplit = apiVersion.replace("-SNAPSHOT", "").split("\\.");
        final String minor = apiSplit.length > 1 ? apiSplit[1] : apiSplit.length > 0 ?  apiSplit[apiSplit.length - 1] : "-1";
        final String apiReleaseVersion = apiSplit[0] + '.' + minor;
        return Stream.of(minecraftVersion, addedVersionInfo, apiReleaseVersion + '.' + implRecommendedVersion)
            .filter(Objects::nonNull)
            .collect(Collectors.joining("-"));
    }

    public String generatePlatformBuildVersionString(final String apiVersion, final String minecraftVersion, final String implRecommendedVersion) {
        return this.generatePlatformBuildVersionString(apiVersion, minecraftVersion, implRecommendedVersion, null);
    }

    public String generatePlatformBuildVersionString(final String apiVersion, final String minecraftVersion, final String implRecommendedVersion, final String addedVersionInfo) {
        final boolean isRelease = !implRecommendedVersion.endsWith("-SNAPSHOT");

        this.logger.lifecycle("Detected Implementation Version {} as {}", implRecommendedVersion, isRelease ? "Release" : "Snapshot");
        final String[] apiSplit = apiVersion.replace("-SNAPSHOT", "").split("\\.");
        final String minor = apiSplit.length > 1 ? apiSplit[1] : apiSplit.length > 0 ?  apiSplit[apiSplit.length - 1] : "-1";
        final String apiReleaseVersion = apiSplit[0] + '.' + minor;
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

}
