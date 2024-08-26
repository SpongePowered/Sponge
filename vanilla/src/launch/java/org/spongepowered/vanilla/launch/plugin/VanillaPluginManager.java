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
import org.apache.logging.log4j.Level;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.launch.plugin.SpongePluginManager;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.plugin.InvalidPluginException;
import org.spongepowered.plugin.PluginCandidate;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.PluginLanguageService;
import org.spongepowered.plugin.PluginLoader;
import org.spongepowered.plugin.PluginResource;
import org.spongepowered.plugin.metadata.model.PluginDependency;
import org.spongepowered.vanilla.applaunch.plugin.VanillaPluginPlatform;
import org.spongepowered.vanilla.launch.VanillaLaunch;
import org.spongepowered.vanilla.launch.plugin.resolver.DependencyResolver;
import org.spongepowered.vanilla.launch.plugin.resolver.ResolutionResult;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public final class VanillaPluginManager implements SpongePluginManager {
    private final Map<String, PluginContainer> plugins;
    private final Map<Object, PluginContainer> instancesToPlugins;
    private final List<PluginContainer> sortedPlugins;
    private final Map<PluginContainer, PluginResource> containerToResource;

    public VanillaPluginManager() {
        this.plugins = new Object2ObjectOpenHashMap<>();
        this.instancesToPlugins = new IdentityHashMap<>();
        this.sortedPlugins = new ArrayList<>();
        this.containerToResource = new Object2ObjectOpenHashMap<>();
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
        final Map<PluginCandidate<PluginResource>, PluginLanguageService<PluginResource>> pluginLanguageLookup = new HashMap<>();
        final Map<PluginLanguageService<PluginResource>, PluginLoader<PluginResource, PluginContainer>> pluginLoaders = new HashMap<>();

        // Initialise the plugin language loaders.
        for (final Map.Entry<PluginLanguageService<PluginResource>, List<PluginCandidate<PluginResource>>> candidate : platform.getCandidates().entrySet()) {
            final PluginLanguageService<PluginResource> languageService = candidate.getKey();
            final String loaderClass = languageService.pluginLoader();
            try {
                pluginLoaders.put(languageService,
                        (PluginLoader<PluginResource, PluginContainer>) Class.forName(loaderClass).getConstructor().newInstance());
            } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            candidate.getValue().forEach(x -> pluginLanguageLookup.put(x, languageService));
        }

        // Priority to platform plugins that will already exist here -- meaning the resolver will act upon them first
        // and if someone decides to give a plugin an ID that is the same as a platform plugin, the resolver will effectively
        // reject it.
        final Set<PluginCandidate<PluginResource>> resources = new LinkedHashSet<>();
        pluginLanguageLookup.keySet().stream().filter(x -> this.plugins.containsKey(x.metadata().id())).forEach(resources::add);
        resources.addAll(pluginLanguageLookup.keySet());

        final ResolutionResult<PluginResource> resolutionResult = DependencyResolver.resolveAndSortCandidates(resources, platform.logger());
        final Map<PluginCandidate<PluginResource>, String> failedInstances = new HashMap<>();
        final Map<PluginCandidate<PluginResource>, String> consequentialFailedInstances = new HashMap<>();
        final ClassLoader launchClassloader = VanillaLaunch.instance().getClass().getClassLoader();
        for (final PluginCandidate<PluginResource> candidate : resolutionResult.sortedSuccesses()) {
            final PluginContainer plugin = this.plugins.get(candidate.metadata().id());
            if (plugin != null) {
                if (plugin instanceof VanillaDummyPluginContainer) {
                    continue;
                }
                // If we get here, we screwed up - duplicate IDs should have been detected earlier.
                // Place it in the resolution result... it'll then get picked up in the big error message
                resolutionResult.duplicateIds().add(candidate.metadata().id());

                // but this is our screw up, let's also make a big point of it
                final PrettyPrinter prettyPrinter = new PrettyPrinter(120)
                        .add("ATTEMPTED TO CREATE PLUGIN WITH DUPLICATE PLUGIN ID").centre()
                        .hr()
                        .addWrapped("Sponge attempted to create a second plugin with ID '%s'. This is not allowed - all plugins must have a unique "
                                        + "ID. Usually, Sponge will catch this earlier -- but in this case Sponge has validated two plugins with "
                                        + "the same ID. Please report this error to Sponge.",
                                candidate.metadata().id())
                        .add()
                        .add("Technical Details:")
                        .add("Plugins to load:", 4);
                resolutionResult.sortedSuccesses().forEach(x -> prettyPrinter.add("*" + x.metadata().id(), 4));
                prettyPrinter.add().add("Detected Duplicate IDs:", 4);
                resolutionResult.duplicateIds().forEach(x -> prettyPrinter.add("*" + x, 4));
                prettyPrinter.log(platform.logger(), Level.ERROR);
                continue;
            }

            // If a dependency failed to load, then we should bail on required dependencies too.
            // This should work fine, we're sorted so all deps should be in place at this stage.
            if (this.stillValid(candidate, consequentialFailedInstances)) {
                final PluginLanguageService<PluginResource> languageService = pluginLanguageLookup.get(candidate);
                final PluginLoader<PluginResource, PluginContainer> pluginLoader = pluginLoaders.get(languageService);
                try {
                    final PluginContainer container = pluginLoader.loadPlugin(platform.getStandardEnvironment(), candidate, launchClassloader);
                    this.addPlugin(container);
                    this.containerToResource.put(container, candidate.resource());
                } catch (final InvalidPluginException e) {
                    failedInstances.put(candidate, "Failed to construct: see stacktrace(s) above this message for details.");
                    platform.logger().error("Failed to construct plugin {}", candidate.metadata().id(), e);
                }
            }
        }

        resolutionResult.printErrorsIfAny(failedInstances, consequentialFailedInstances, platform.logger());
        platform.logger().info("Loaded plugin(s): {}", this.sortedPlugins.stream().map(p -> p.metadata().id()).collect(Collectors.toList()));
    }

    public void addPlugin(final PluginContainer plugin) {
        this.plugins.put(plugin.metadata().id(), Objects.requireNonNull(plugin, "plugin"));
        this.sortedPlugins.add(plugin);

        if (!(plugin instanceof VanillaDummyPluginContainer)) {
            this.instancesToPlugins.put(plugin.instance(), plugin);
        }
    }

    @Nullable
    public PluginResource resource(final PluginContainer container) {
        return this.containerToResource.get(container);
    }

    private boolean stillValid(final PluginCandidate<PluginResource> candidate, final Map<PluginCandidate<PluginResource>, String> consequential) {
        final Optional<PluginDependency> failedId =
                candidate.metadata().dependencies().stream().filter(x -> !x.optional() && !this.plugins.containsKey(x.id())).findFirst();
        if (failedId.isPresent()) {
            consequential.put(candidate, failedId.get().id());
            return false;
        }
        return true;
    }

}
