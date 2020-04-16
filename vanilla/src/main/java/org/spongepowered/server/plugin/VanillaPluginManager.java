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
package org.spongepowered.server.plugin;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.plugin.meta.PluginMetadata;
import org.spongepowered.server.SpongeVanilla;
import org.spongepowered.server.launch.plugin.PluginCandidate;
import org.spongepowered.server.launch.plugin.PluginSource;
import org.spongepowered.server.launch.plugin.VanillaLaunchPluginManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Singleton
public class VanillaPluginManager implements PluginManager {

    private final Injector rootInjector;

    private final Map<String, PluginContainer> plugins = new HashMap<>();
    private final Map<Object, PluginContainer> pluginInstances = new IdentityHashMap<>();

    @Inject
    public VanillaPluginManager(Injector injector, SpongeVanilla impl, MetadataContainer metadata) {
        this.rootInjector = injector.getParent();

        this.registerPlugin(SpongeImpl.getMinecraftPlugin());
        this.registerPlugin(SpongeImpl.getSpongePlugin());
        this.registerPlugin(metadata.createContainer(Platform.API_ID, SpongeImpl.API_NAME, impl.getSource()));
        this.registerPlugin(impl);
        this.registerPlugin(metadata.createContainer("mcp", "Mod Coder Pack", impl.getSource()));
    }

    private void registerPlugin(PluginContainer plugin) {
        this.plugins.put(plugin.getId(), plugin);
        plugin.getInstance().ifPresent(instance -> this.pluginInstances.put(instance, plugin));
    }

    public void loadPlugins() throws IOException {
        Map<String, PluginCandidate> candidateMap = VanillaLaunchPluginManager.getPlugins();
        if (candidateMap.isEmpty()) {
            return; // Nothing to do
        }

        try {
            PluginSorter.sort(checkRequirements(candidateMap)).forEach(this::loadPlugin);
        } catch (Throwable e) {
            throw PluginReporter.crash(e, candidateMap.values());
        }
    }

    private Set<PluginCandidate> checkRequirements(Map<String, PluginCandidate> candidates) {
        // Collect all versions of already loaded plugins
        Map<String, String> loadedPlugins = new HashMap<>();
        for (PluginContainer container : this.plugins.values()) {
            loadedPlugins.put(container.getId(), container.getVersion().orElse(null));
        }


        Set<PluginCandidate> successfulCandidates = new HashSet<>(candidates.size());
        List<PluginCandidate> failedCandidates = new ArrayList<>();

        for (PluginCandidate candidate : candidates.values()) {
            if (candidate.collectDependencies(loadedPlugins, candidates)) {
                successfulCandidates.add(candidate);
            } else {
                failedCandidates.add(candidate);
            }
        }

        if (failedCandidates.isEmpty()) {
            return successfulCandidates; // Nothing to do, all requirements satisfied
        }

        PluginCandidate candidate;
        boolean updated;
        while (true) {
            updated = false;
            Iterator<PluginCandidate> itr = successfulCandidates.iterator();
            while (itr.hasNext()) {
                candidate = itr.next();
                if (candidate.updateRequirements()) {
                    updated = true;
                    itr.remove();
                    failedCandidates.add(candidate);
                }
            }

            if (updated) {
                // Update failed candidates as well
                failedCandidates.forEach(PluginCandidate::updateRequirements);
            } else {
                break;
            }
        }

        for (PluginCandidate failed : failedCandidates) {
            if (failed.isInvalid()) {
                SpongeImpl.getLogger().error("Plugin '{}' from {} cannot be loaded because it is invalid", failed.getId(), failed.getSource());
            } else {
                SpongeImpl.getLogger().error("Cannot load plugin '{}' from {} because it is missing the required dependencies {}",
                        failed.getId(), failed.getSource(), PluginReporter.formatRequirements(failed.getMissingRequirements()));
            }
        }

        return successfulCandidates;
    }

    private void loadPlugin(PluginCandidate candidate) {
        final String id = candidate.getId();
        candidate.getSource().addToClasspath();

        final PluginMetadata metadata = candidate.getMetadata();
        final String name = firstNonNull(metadata.getName(), id);
        final String version = firstNonNull(metadata.getVersion(), "unknown");

        try {
            Class<?> pluginClass = Class.forName(candidate.getPluginClass());
            Optional<Path> source = candidate.getSource().getPath();
            if (!source.isPresent()) {
                source = PluginSource.find(pluginClass);
            }
            PluginContainer container = new VanillaPluginContainer(this.rootInjector, pluginClass, metadata, source);

            registerPlugin(container);
            Sponge.getEventManager().registerListeners(container, container.getInstance().get());

            SpongeImpl.getLogger().info("Loaded plugin: {} {} (from {})", name, version, candidate.getSource());
        } catch (Throwable e) {
            SpongeImpl.getLogger().error("Failed to load plugin: {} {} (from {})", name, version, candidate.getSource(), e);
        }
    }

    @Override
    public Optional<PluginContainer> fromInstance(Object instance) {
        checkNotNull(instance, "instance");

        if (instance instanceof PluginContainer) {
            return Optional.of((PluginContainer) instance);
        }

        return Optional.ofNullable(this.pluginInstances.get(instance));
    }

    @Override
    public Optional<PluginContainer> getPlugin(String id) {
        return Optional.ofNullable(this.plugins.get(id));
    }

    @Override
    public Collection<PluginContainer> getPlugins() {
        return Collections.unmodifiableCollection(this.plugins.values());
    }

    @Override
    public boolean isLoaded(String id) {
        return this.plugins.containsKey(id);
    }

}
