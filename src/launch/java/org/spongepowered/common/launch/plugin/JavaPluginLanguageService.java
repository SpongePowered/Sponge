/*
 * This file is part of plugin-spi, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.launch.plugin;

import com.google.inject.Injector;
import org.spongepowered.common.launch.LauncherConstants;
import org.spongepowered.common.launch.plugin.config.PluginMetadataConfiguration;
import org.spongepowered.common.launch.plugin.config.section.ContributorSection;
import org.spongepowered.common.launch.plugin.config.section.DependencySection;
import org.spongepowered.common.launch.plugin.config.section.LinksSection;
import org.spongepowered.common.launch.plugin.config.section.PluginSection;
import org.spongepowered.plugin.PluginCandidate;
import org.spongepowered.plugin.PluginEnvironment;
import org.spongepowered.plugin.PluginKeys;
import org.spongepowered.plugin.jvm.JVMPluginLanguageService;
import org.spongepowered.plugin.metadata.PluginContributor;
import org.spongepowered.plugin.metadata.PluginDependency;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.plugin.metadata.PluginMetadataContainer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class JavaPluginLanguageService extends JVMPluginLanguageService {

    private final static String NAME = "java_sponge";

    @Override
    public String getName() {
        return JavaPluginLanguageService.NAME;
    }

    @Override
    public String getPluginMetadataFileName() {
        return LauncherConstants.Plugin.Metadata.FILENAME;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<PluginMetadataContainer> createPluginMetadata(final PluginEnvironment environment, final String filename, final InputStream stream) {
        final PluginMetadataConfiguration configuration;
        try {
            configuration = PluginMetadataConfiguration.loadFrom(environment, filename, stream);
        } catch (final Exception e) {
            environment.getLogger().error("Encountered an issue reading plugin metadata!", e);
            return Optional.empty();
        }

        final List<PluginMetadata> pluginMetadata = new ArrayList<>();
        for (final PluginSection pluginSection : configuration.getPluginSections()) {
            final PluginMetadata.Builder metadataBuilder = PluginMetadata.builder();
            metadataBuilder
                .setId(pluginSection.getId())
                .setName(pluginSection.getName())
                .setVersion(pluginSection.getVersion())
                .setMainClass(pluginSection.getMainClass())
                .setDescription(pluginSection.getDescription());

            final LinksSection linksSection = pluginSection.getLinksSection();
            if (linksSection != null) {
                metadataBuilder.setHomepage(linksSection.getHomepage());
                metadataBuilder.setSource(linksSection.getSource());
                metadataBuilder.setIssues(linksSection.getIssues());
            }

            final List<ContributorSection> contributorSections = pluginSection.getContributorSections();
            final List<PluginContributor> pluginContributors = new ArrayList<>();

            if (contributorSections != null) {
                for (final ContributorSection contributorSection : contributorSections) {
                    final String name = contributorSection.getName();
                    if (name == null || name.isEmpty()) {
                        environment.getLogger().error("Plugin '{}' cannot specify a developer with no name! Skipping...", pluginSection.getId());
                        continue;
                    }

                    final PluginContributor.Builder developerBuilder = PluginContributor.builder()
                        .setName(contributorSection.getName())
                        .setDescription(contributorSection.getDescription());

                    pluginContributors.add(developerBuilder.build());
                }
            }

            if (pluginContributors.isEmpty()) {
                environment.getLogger().error("Plugin '{}' must specify at least one contributor! Skipping...", pluginSection.getId());
                continue;
            }

            metadataBuilder.setContributors(pluginContributors);

            final List<DependencySection> dependencySections = pluginSection.getDependencySections();
            final List<PluginDependency> pluginDependencies = new ArrayList<>();

            if (dependencySections != null) {
                for (final DependencySection dependencySection : dependencySections) {
                    final String id = dependencySection.getId();
                    if (id == null) {
                        environment.getLogger().error("Plugin '{}' cannot specify a dependency with no id! Skipping...", pluginSection.getId());
                        continue;
                    }

                    final String version = dependencySection.getVersion();
                    if (version == null) {
                        environment.getLogger().error("Plugin '{}' cannot specify a dependency with no version!", pluginSection.getId());
                        continue;
                    }

                    final PluginDependency.Builder dependencyBuilder = PluginDependency.builder()
                        .setId(id)
                        .setVersion(version);

                    pluginDependencies.add(dependencyBuilder.build());
                }
            }

            metadataBuilder.setDependencies(pluginDependencies);

            final Map<String, String> extraMetadata = pluginSection.getExtraMetadata();
            if (extraMetadata != null) {
                metadataBuilder.setExtraMetadata((Map<String, Object>) (Object) extraMetadata);
            }

            pluginMetadata.add(metadataBuilder.build());
        }

        return Optional.of(PluginMetadataContainer.of(pluginMetadata));
    }

    @Override
    protected List<PluginCandidate> sortCandidates(final List<PluginCandidate> pluginCandidates) {
        // TODO Sort based on dependencies..
        return pluginCandidates;
    }

    @Override
    protected Object createPluginInstance(final PluginEnvironment environment, final PluginCandidate candidate, final ClassLoader targetClassLoader) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        final String mainClass = candidate.getMetadata().getMainClass();
        final Class<?> pluginClass = Class.forName(mainClass, true, targetClassLoader);
        final Injector parentInjector = environment.getBlackboard().get(PluginKeys.PARENT_INJECTOR).orElse(null);
        if (parentInjector != null) {
            final Injector childInjector = parentInjector.createChildInjector(new PluginModule());
            return childInjector.getInstance(pluginClass);
        }
        return pluginClass.newInstance();
    }
}
