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
package org.spongepowered.vanilla.modlauncher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import org.spongepowered.common.launch.plugin.PluginLoader;
import org.spongepowered.plugin.PluginEnvironment;
import org.spongepowered.plugin.PluginKeys;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public final class PluginDiscovererService implements ITransformationService {

    private static final String NAME = "plugin_discoverer";
    private final PluginEnvironment pluginEnvironment;

    private PluginLoader pluginLoader;

    public PluginDiscovererService() {
        this.pluginEnvironment = Main.getLaunchPluginEnvironment();
    }

    @Nonnull
    @Override
    public String name() {
        return PluginDiscovererService.NAME;
    }

    @Override
    public void initialize(final IEnvironment environment) {
        this.pluginLoader.initialize();
    }

    @Override
    public void beginScanning(final IEnvironment environment) {
        //NOOP
    }

    @Override
    public List<Map.Entry<String, Path>> runScan(final IEnvironment environment) {
        this.pluginLoader.discoverResources();

        final List<Map.Entry<String, Path>> launchResources = new ArrayList<>();

        for (final Map.Entry<String, Collection<Path>> resourcesEntry : this.pluginLoader.getResources().entrySet()) {
            final Collection<Path> resources = resourcesEntry.getValue();
            launchResources.addAll(
                resources
                    .stream()
                    .map(pluginFile -> Maps.immutableEntry(pluginFile.getFileName().toString(), pluginFile))
                    .collect(Collectors.toList())
            );
        }

        return launchResources;
    }

    @Override
    public void onLoad(final IEnvironment env, final Set<String> otherServices) {
        this.pluginEnvironment.getLogger().info("SpongePowered PLUGIN Subsystem Version={} Service=ModLauncher", this.pluginEnvironment.getBlackboard().get(PluginKeys.VERSION).get());
        this.pluginLoader = new PluginLoader(this.pluginEnvironment, null);
        this.pluginLoader.discoverServices();
        this.pluginLoader.getServices().forEach((k, v) -> this.pluginLoader.getEnvironment().getLogger().info("Plugin language loader '{}' found.", k));
    }

    @Nonnull
    @Override
    public List<ITransformer> transformers() {
        return ImmutableList.of();
    }
}
