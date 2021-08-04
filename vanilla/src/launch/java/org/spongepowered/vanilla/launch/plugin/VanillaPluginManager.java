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
package org.spongepowered.vanilla.launch.plugin;

import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.spongepowered.common.launch.plugin.SpongePluginManager;
import org.spongepowered.plugin.InvalidPluginException;
import org.spongepowered.plugin.PluginCandidate;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.PluginLanguageService;
import org.spongepowered.plugin.PluginLoader;
import org.spongepowered.plugin.PluginResource;
import org.spongepowered.vanilla.applaunch.plugin.VanillaPluginPlatform;
import org.spongepowered.vanilla.launch.VanillaLaunch;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public final class VanillaPluginManager implements SpongePluginManager {

    private final Map<String, PluginContainer> plugins;
    private final Map<Object, PluginContainer> instancesToPlugins;
    private final List<PluginContainer> sortedPlugins;

    public VanillaPluginManager() {
        this.plugins = new Object2ObjectOpenHashMap<>();
        this.instancesToPlugins = new IdentityHashMap<>();
        this.sortedPlugins = new ArrayList<>();
    }

    @Override
    public Optional<PluginContainer> fromInstance(final Object instance) {
        return Optional.ofNullable(this.instancesToPlugins.get(Objects.requireNonNull(instance, "instance")));
    }

    @Override
    public Optional<PluginContainer> plugin(final String id) {
        return Optional.ofNullable(this.plugins.get(Objects.requireNonNull(id, "id")));
    }

    @Override
    public Collection<PluginContainer> plugins() {
        return Collections.unmodifiableCollection(this.sortedPlugins);
    }

    public void loadPlugins(final VanillaPluginPlatform platform) {
        for (final Map.Entry<PluginLanguageService<PluginResource>, List<PluginCandidate<PluginResource>>> languageCandidates : platform.getCandidates().entrySet()) {
            final PluginLanguageService<PluginResource> languageService = languageCandidates.getKey();
            final Collection<PluginCandidate<PluginResource>> candidates = languageCandidates.getValue();
            final String loaderClass = languageService.pluginLoader();
            final PluginLoader<PluginResource, PluginContainer> pluginLoader;
            try {
                pluginLoader =  (PluginLoader<PluginResource, PluginContainer>) Class.forName(loaderClass).getConstructor().newInstance();
            } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            for (final PluginCandidate<PluginResource> candidate : candidates) {
                PluginContainer plugin = this.plugins.get(candidate.metadata().id());
                if (plugin != null) {
                    if (plugin instanceof VanillaDummyPluginContainer) {
                        continue;
                    }
                    // TODO Print nasty message or do something about the dupe otherwise?
                    continue;
                }

                plugin = pluginLoader.createPluginContainer(candidate, platform.getPluginEnvironment()).orElse(null);
                if (plugin == null) {
                    platform.logger().debug("Language service '{}' returned a null plugin container for '{}'.",
                            languageService.name(), candidate.metadata().id());
                    continue;
                }

                try {
                    pluginLoader.loadPlugin(platform.getPluginEnvironment(), plugin, VanillaLaunch.instance().getClass().getClassLoader());
                    this.addPlugin(plugin);
                } catch (final InvalidPluginException e) {
                    e.printStackTrace();
                }
            }
        }

        platform.logger().info("Loaded plugin(s): {}", this.sortedPlugins.stream().map(p -> p.metadata().id()).collect(Collectors.toList()));
    }

    public void addPlugin(final PluginContainer plugin) {
        this.plugins.put(plugin.metadata().id(), Objects.requireNonNull(plugin, "plugin"));
        this.sortedPlugins.add(plugin);

        if (!(plugin instanceof VanillaDummyPluginContainer)) {
            this.instancesToPlugins.put(plugin.instance(), plugin);
        }
    }
}
