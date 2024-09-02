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
import org.spongepowered.common.applaunch.plugin.PluginPlatform;
import org.spongepowered.plugin.PluginCandidate;
import org.spongepowered.plugin.PluginLanguageService;
import org.spongepowered.plugin.PluginResource;
import org.spongepowered.plugin.PluginResourceLocatorService;
import org.spongepowered.plugin.blackboard.Blackboard;
import org.spongepowered.plugin.blackboard.Keys;
import org.spongepowered.plugin.builtin.StandardEnvironment;
import org.spongepowered.plugin.builtin.jvm.JVMKeys;
import org.spongepowered.vanilla.applaunch.plugin.resource.SecureJarPluginResource;

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

    private final StandardEnvironment standardEnvironment;
    private final Map<String, PluginResourceLocatorService<?>> locatorServices;
    private final Map<String, PluginLanguageService> languageServices;

    private final Map<String, Set<? extends PluginResource>> locatorResources;
    private final Map<PluginLanguageService, List<PluginCandidate>> pluginCandidates;

    public VanillaPluginPlatform(final StandardEnvironment standardEnvironment) {
        this.standardEnvironment = standardEnvironment;
        this.locatorServices = new HashMap<>();
        this.languageServices = new HashMap<>();
        this.locatorResources = new HashMap<>();
        this.pluginCandidates = new IdentityHashMap<>();
    }

    @Override
    public String version() {
        return this.standardEnvironment.blackboard().get(Keys.VERSION);
    }

    @Override
    public void setVersion(final String version) {
        this.standardEnvironment.blackboard().set(Keys.VERSION, version);
    }

    @Override
    public Logger logger() {
        return this.standardEnvironment.logger();
    }

    @Override
    public Path baseDirectory() {
        return this.standardEnvironment.blackboard().get(Keys.BASE_DIRECTORY);
    }

    @Override
    public void setBaseDirectory(final Path baseDirectory) {
        this.standardEnvironment.blackboard().set(Keys.BASE_DIRECTORY, baseDirectory);
    }

    @Override
    public List<Path> pluginDirectories() {
        return this.standardEnvironment.blackboard().get(Keys.PLUGIN_DIRECTORIES);
    }

    @Override
    public void setPluginDirectories(final List<Path> pluginDirectories) {
        this.standardEnvironment.blackboard().set(Keys.PLUGIN_DIRECTORIES, pluginDirectories);
    }

    @Override
    public String metadataFilePath() {
        return this.standardEnvironment.blackboard().get(Keys.METADATA_FILE_PATH);
    }

    @Override
    public void setMetadataFilePath(final String metadataFilePath) {
        this.standardEnvironment.blackboard().set(Keys.METADATA_FILE_PATH, metadataFilePath);
    }

    public StandardEnvironment getStandardEnvironment() {
        return this.standardEnvironment;
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
            entry.getValue().initialize(this.standardEnvironment);
        }
    }

    public void discoverLocatorServices() {
        final Blackboard blackboard = this.standardEnvironment.blackboard();
        blackboard.set(JVMKeys.ENVIRONMENT_LOCATOR_VARIABLE_NAME, "SPONGE_PLUGINS");
        blackboard.set(JVMKeys.JVM_PLUGIN_RESOURCE_FACTORY, SecureJarPluginResource::new);

        final ModuleLayer serviceLayer = Launcher.INSTANCE.environment().findModuleLayerManager().flatMap(lm -> lm.getLayer(IModuleLayerManager.Layer.SERVICE)).orElseThrow();
        final var serviceLoader = ServiceLoader.load(serviceLayer, PluginResourceLocatorService.class);

        for (final var iter = serviceLoader.iterator(); iter.hasNext(); ) {
            final PluginResourceLocatorService<?> next;

            try {
                next = iter.next();
            } catch (final ServiceConfigurationError e) {
                this.standardEnvironment.logger().error("Error encountered initializing plugin resource locator!", e);
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
                this.standardEnvironment.logger().error("Error encountered initializing plugin language service!", e);
                continue;
            }

            this.languageServices.put(next.name(), next);
        }
    }

    public void locatePluginResources() {
        for (final Map.Entry<String, PluginResourceLocatorService<?>> locatorEntry : this.locatorServices.entrySet()) {
            final PluginResourceLocatorService<?> locatorService = locatorEntry.getValue();
            final Set<? extends PluginResource> resources = locatorService.locatePluginResources(this.standardEnvironment);
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
                        final List<PluginCandidate> candidates = languageService.createPluginCandidates(this.standardEnvironment, pluginResource);
                        if (candidates.isEmpty()) {
                            continue;
                        }
                        this.pluginCandidates.computeIfAbsent(languageService, k -> new LinkedList<>()).addAll(candidates);

                        if (pluginResource instanceof SecureJarPluginResource jarResource) {
                            jarResource.addCandidates(candidates);
                        }
                    } catch (final Exception ex) {
                        this.standardEnvironment.logger().error("Failed to create plugin candidates", ex);
                    }
                }
            }
        }
    }
}
