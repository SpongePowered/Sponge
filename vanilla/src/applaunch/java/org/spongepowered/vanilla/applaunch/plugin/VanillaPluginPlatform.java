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

import org.apache.logging.log4j.Logger;
import org.spongepowered.common.applaunch.plugin.PluginPlatform;
import org.spongepowered.plugin.PluginCandidate;
import org.spongepowered.plugin.PluginLanguageService;
import org.spongepowered.plugin.PluginResource;
import org.spongepowered.plugin.PluginResourceLocatorService;
import org.spongepowered.plugin.blackboard.Keys;
import org.spongepowered.plugin.builtin.StandardEnvironment;
import org.spongepowered.plugin.builtin.jvm.JVMKeys;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

public final class VanillaPluginPlatform implements PluginPlatform {

    private final StandardEnvironment standardEnvironment;
    private final Map<String, PluginResourceLocatorService<PluginResource>> locatorServices;
    private final Map<String, PluginLanguageService<PluginResource>> languageServices;

    private final Map<String, Set<PluginResource>> locatorResources;
    private final Map<PluginLanguageService<PluginResource>, List<PluginCandidate<PluginResource>>> pluginCandidates;

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
        this.standardEnvironment.blackboard().getOrCreate(Keys.VERSION, () -> version);
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
        this.standardEnvironment.blackboard().getOrCreate(Keys.BASE_DIRECTORY, () -> baseDirectory);
    }

    @Override
    public List<Path> pluginDirectories() {
        return this.standardEnvironment.blackboard().get(Keys.PLUGIN_DIRECTORIES);
    }

    @Override
    public void setPluginDirectories(final List<Path> pluginDirectories) {
        this.standardEnvironment.blackboard().getOrCreate(Keys.PLUGIN_DIRECTORIES, () -> pluginDirectories);
    }

    @Override
    public String metadataFilePath() {
        return this.standardEnvironment.blackboard().get(JVMKeys.METADATA_FILE_PATH);
    }

    @Override
    public void setMetadataFilePath(final String metadataFilePath) {
        this.standardEnvironment.blackboard().getOrCreate(JVMKeys.METADATA_FILE_PATH, () -> metadataFilePath);
    }

    public StandardEnvironment getStandardEnvironment() {
        return this.standardEnvironment;
    }

    public Map<String, PluginResourceLocatorService<PluginResource>> getLocatorServices() {
        return Collections.unmodifiableMap(this.locatorServices);
    }

    public Map<String, PluginLanguageService<PluginResource>> getLanguageServices() {
        return Collections.unmodifiableMap(this.languageServices);
    }

    public Map<String, Set<PluginResource>> getResources() {
        return Collections.unmodifiableMap(this.locatorResources);
    }

    public Map<PluginLanguageService<PluginResource>, List<PluginCandidate<PluginResource>>> getCandidates() {
        return Collections.unmodifiableMap(this.pluginCandidates);
    }

    public void initialize() {
        for (final Map.Entry<String, PluginLanguageService<PluginResource>> entry : this.languageServices.entrySet()) {
            entry.getValue().initialize(this.standardEnvironment);
        }
    }

    public void discoverLocatorServices() {
        final ServiceLoader<PluginResourceLocatorService<PluginResource>> serviceLoader = (ServiceLoader<PluginResourceLocatorService<PluginResource>>) (Object) ServiceLoader.load(
                PluginResourceLocatorService.class, null);

        for (final Iterator<PluginResourceLocatorService<PluginResource>> iter = serviceLoader.iterator(); iter.hasNext(); ) {
            final PluginResourceLocatorService<PluginResource> next;

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
        final ServiceLoader<PluginLanguageService<PluginResource>> serviceLoader = (ServiceLoader<PluginLanguageService<PluginResource>>) (Object) ServiceLoader.load(
                PluginLanguageService.class, VanillaPluginPlatform.class.getClassLoader());

        for (final Iterator<PluginLanguageService<PluginResource>> iter = serviceLoader.iterator(); iter.hasNext(); ) {
            final PluginLanguageService<PluginResource> next;

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
        for (final Map.Entry<String, PluginResourceLocatorService<PluginResource>> locatorEntry : this.locatorServices.entrySet()) {
            final PluginResourceLocatorService<PluginResource> locatorService = locatorEntry.getValue();
            final Set<PluginResource> resources = locatorService.locatePluginResources(this.standardEnvironment);
            if (!resources.isEmpty()) {
                this.locatorResources.put(locatorEntry.getKey(), resources);
            }
        }
    }

    public void createPluginCandidates() {
        for (final Map.Entry<String, PluginLanguageService<PluginResource>> languageEntry : this.languageServices.entrySet()) {
            final PluginLanguageService<PluginResource> languageService = languageEntry.getValue();
            for (final Map.Entry<String, Set<PluginResource>> resourcesEntry : this.locatorResources.entrySet()) {

                for (final PluginResource pluginResource : resourcesEntry.getValue()) {
                    try {
                        final List<PluginCandidate<PluginResource>> candidates = languageService.createPluginCandidates(this.standardEnvironment,
                                pluginResource);
                        if (candidates.isEmpty()) {
                            continue;
                        }
                        this.pluginCandidates.computeIfAbsent(languageService, k -> new LinkedList<>()).addAll(candidates);
                    } catch (final Exception ex) {
                        this.standardEnvironment.logger().error("Failed to create plugin candidates", ex);
                    }
                }
            }
        }
    }
}
