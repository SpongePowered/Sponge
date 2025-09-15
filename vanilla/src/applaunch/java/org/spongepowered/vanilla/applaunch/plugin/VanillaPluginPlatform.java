/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
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
package org.spongepowered.vanilla.applaunch.plugin;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.common.applaunch.config.LaunchConfig;
import org.spongepowered.common.applaunch.config.TokenReplacement;
import org.spongepowered.common.applaunch.plugin.PluginPlatform;
import org.spongepowered.common.applaunch.plugin.PluginPlatformConstants;
import org.spongepowered.plugin.Environment;
import org.spongepowered.plugin.PluginCandidate;
import org.spongepowered.plugin.PluginLanguageService;
import org.spongepowered.plugin.PluginResource;
import org.spongepowered.plugin.PluginResourceLocatorService;
import org.spongepowered.plugin.blackboard.Blackboard;
import org.spongepowered.plugin.blackboard.Keys;
import org.spongepowered.plugin.builtin.StandardEnvironment;
import org.spongepowered.plugin.builtin.jvm.JVMKeys;
import org.spongepowered.vanilla.applaunch.plugin.resource.SecureJarPluginResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

public final class VanillaPluginPlatform implements PluginPlatform {

    private final Environment environment;
    private final LaunchConfig config;
    private final TokenReplacement tokens;

    private final Map<String, PluginResourceLocatorService<?>> locatorServices;
    private final Map<String, PluginLanguageService> languageServices;

    private final Map<String, Set<? extends PluginResource>> locatorResources;
    private final Map<PluginLanguageService, List<PluginCandidate>> pluginCandidates;

    public VanillaPluginPlatform(final Path baseDirectory) throws IOException {
        this.environment = new StandardEnvironment();

        final String implVersion = StandardEnvironment.class.getPackage().getImplementationVersion();
        this.setVersion(implVersion == null ? "dev" : implVersion);

        this.setBaseDirectory(baseDirectory);
        this.setMetadataFilePath(PluginPlatformConstants.METADATA_FILE_LOCATION);

        this.config = LaunchConfig.load(baseDirectory, false);

        final Path modsDirectory = baseDirectory.resolve("mods");
        this.tokens = new TokenReplacement();
        this.tokens.register("BASE_DIR", baseDirectory);
        this.tokens.register("CONFIG_DIR", this.configDirectory());
        this.tokens.register("MODS_DIR", modsDirectory);

        this.locatorServices = new HashMap<>();
        this.languageServices = new HashMap<>();
        this.locatorResources = new HashMap<>();
        this.pluginCandidates = new IdentityHashMap<>();

        final Path additionalPluginsDirectory = Path.of(this.tokens.replace(this.config.additionalPluginsDirectory()));
        Files.createDirectories(additionalPluginsDirectory);
        this.setPluginDirectories(List.of(modsDirectory, additionalPluginsDirectory));
    }

    @Override
    public String version() {
        return this.environment.blackboard().get(Keys.VERSION);
    }

    private void setVersion(final String version) {
        this.environment.blackboard().set(Keys.VERSION, version);
    }

    @Override
    public Logger logger() {
        return this.environment.logger();
    }

    @Override
    public boolean vanilla() {
        return true;
    }

    @Override
    public Path baseDirectory() {
        return this.environment.blackboard().get(Keys.BASE_DIRECTORY);
    }

    @Override
    public Path configDirectory() {
        return this.baseDirectory().resolve("config");
    }

    @Override
    public LaunchConfig config() {
        return this.config;
    }

    @Override
    public TokenReplacement tokens() {
        return this.tokens;
    }

    private void setBaseDirectory(final Path baseDirectory) {
        this.environment.blackboard().set(Keys.BASE_DIRECTORY, baseDirectory);
    }

    @Override
    public List<Path> pluginDirectories() {
        return this.environment.blackboard().get(Keys.PLUGIN_DIRECTORIES);
    }

    private void setPluginDirectories(final List<Path> pluginDirectories) {
        this.environment.blackboard().set(Keys.PLUGIN_DIRECTORIES, pluginDirectories);
    }

    public String metadataFilePath() {
        return this.environment.blackboard().get(Keys.METADATA_FILE_PATH);
    }

    private void setMetadataFilePath(final String metadataFilePath) {
        this.environment.blackboard().set(Keys.METADATA_FILE_PATH, metadataFilePath);
    }

    public Environment getEnvironment() {
        return this.environment;
    }

    public Map<String, PluginResourceLocatorService<?>> getLocatorServices() {
        return Collections.unmodifiableMap(this.locatorServices);
    }

    public Map<String, PluginLanguageService> getLanguageServices() {
        return Collections.unmodifiableMap(this.languageServices);
    }

    public Map<String, Set<? extends PluginResource>> getResources() {
        return Collections.unmodifiableMap(this.locatorResources);
    }

    public Map<PluginLanguageService, List<PluginCandidate>> getCandidates() {
        return Collections.unmodifiableMap(this.pluginCandidates);
    }

    public void initializeLanguageServices() {
        for (final Map.Entry<String, PluginLanguageService> entry : this.languageServices.entrySet()) {
            entry.getValue().initialize(this.environment);
        }
    }

    public void discoverLocatorServices() {
        final Blackboard blackboard = this.environment.blackboard();
        blackboard.set(JVMKeys.ENVIRONMENT_LOCATOR_VARIABLE_NAME, "SPONGE_PLUGINS");
        blackboard.set(JVMKeys.JVM_PLUGIN_RESOURCE_FACTORY, SecureJarPluginResource::new);

        final ModuleLayer serviceLayer = Launcher.INSTANCE.environment().findModuleLayerManager().flatMap(lm -> lm.getLayer(IModuleLayerManager.Layer.SERVICE)).orElseThrow();
        final var serviceLoader = ServiceLoader.load(serviceLayer, PluginResourceLocatorService.class);

        for (final var iter = serviceLoader.iterator(); iter.hasNext(); ) {
            final PluginResourceLocatorService<?> next;

            try {
                next = iter.next();
            } catch (final ServiceConfigurationError e) {
                this.environment.logger().error("Error encountered initializing plugin resource locator!", e);
                continue;
            }

            this.locatorServices.put(next.name(), next);
        }
    }

    public void discoverLanguageServices() {
        final ModuleLayer pluginLayer = Launcher.INSTANCE.environment().findModuleLayerManager().flatMap(lm -> lm.getLayer(IModuleLayerManager.Layer.PLUGIN)).orElseThrow();
        final var serviceLoader = ServiceLoader.load(pluginLayer, PluginLanguageService.class);

        for (final var iter = serviceLoader.iterator(); iter.hasNext(); ) {
            final PluginLanguageService next;

            try {
                next = iter.next();
            } catch (final ServiceConfigurationError e) {
                this.environment.logger().error("Error encountered initializing plugin language service!", e);
                continue;
            }

            this.languageServices.put(next.name(), next);
        }
    }

    public void locatePluginResources() {
        for (final Map.Entry<String, PluginResourceLocatorService<?>> locatorEntry : this.locatorServices.entrySet()) {
            final PluginResourceLocatorService<?> locatorService = locatorEntry.getValue();
            final Set<? extends PluginResource> resources = locatorService.locatePluginResources(this.environment);
            if (!resources.isEmpty()) {
                this.locatorResources.put(locatorEntry.getKey(), resources);
            }
        }
    }

    public void createPluginCandidates() {
        for (final PluginLanguageService languageService : this.languageServices.values()) {
            for (final Set<? extends PluginResource> resources : this.locatorResources.values()) {
                for (final PluginResource pluginResource : resources) {
                    if (ResourceType.of(pluginResource) != ResourceType.PLUGIN) {
                        continue;
                    }

                    try {
                        final List<PluginCandidate> candidates = languageService.createPluginCandidates(this.environment, pluginResource);
                        if (candidates.isEmpty()) {
                            continue;
                        }
                        this.pluginCandidates.computeIfAbsent(languageService, k -> new LinkedList<>()).addAll(candidates);

                        if (pluginResource instanceof SecureJarPluginResource jarResource) {
                            jarResource.addCandidates(candidates);
                        }
                    } catch (final Exception ex) {
                        this.environment.logger().error("Failed to create plugin candidates", ex);
                    }
                }
            }
        }
    }
}
