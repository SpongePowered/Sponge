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
package org.spongepowered.vanilla.launch;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import org.spongepowered.common.inject.SpongeCommonModule;
import org.spongepowered.common.inject.SpongeModule;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.launch.mapping.SpongeMappingManager;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.vanilla.applaunch.plugin.VanillaPluginPlatform;
import org.spongepowered.vanilla.launch.inject.SpongeVanillaModule;
import org.spongepowered.vanilla.launch.mapping.VanillaMappingManager;
import org.spongepowered.vanilla.launch.plugin.VanillaDummyPluginContainer;
import org.spongepowered.vanilla.launch.plugin.VanillaPluginManager;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class VanillaLaunch extends Launch {
    private static final Set<String> PLATFORM_IDS = Set.of("spongevanilla", "sponge", "spongeapi", "minecraft");

    private final Stage injectionStage;
    private final VanillaPluginManager pluginManager;
    private final VanillaMappingManager mappingManager;
    private PluginContainer vanillaPlugin;

    protected VanillaLaunch(final VanillaPluginPlatform pluginPlatform, final Stage injectionStage) {
        super(pluginPlatform);
        this.injectionStage = injectionStage;
        this.pluginManager = new VanillaPluginManager();
        this.mappingManager = new VanillaMappingManager();
    }

    @Override
    public final Stage injectionStage() {
        return this.injectionStage;
    }

    @Override
    public final PluginContainer platformPlugin() {
        if (this.vanillaPlugin == null) {
            this.vanillaPlugin = this.pluginManager().plugin("spongevanilla").orElse(null);

            if (this.vanillaPlugin == null) {
                throw new RuntimeException("Could not find the plugin representing SpongeVanilla, this is a serious issue!");
            }
        }

        return this.vanillaPlugin;
    }

    @Override
    public final VanillaPluginPlatform pluginPlatform() {
        return (VanillaPluginPlatform) this.pluginPlatform;
    }

    @Override
    public final VanillaPluginManager pluginManager() {
        return this.pluginManager;
    }

    @Override
    public SpongeMappingManager mappingManager() {
        return this.mappingManager;
    }

    @Override
    public Injector createInjector() {
        final List<Module> modules = Lists.newArrayList(
            new SpongeModule(),
            new SpongeCommonModule(),
            new SpongeVanillaModule()
        );
        return Guice.createInjector(this.injectionStage(), modules);
    }

    protected final void launchPlatform(final String[] args) {
        this.createPlatformPlugins();
        this.logger().info("Loading Sponge, please wait...");
        this.performBootstrap(args);
    }

    protected abstract void performBootstrap(final String[] args);

    protected final void createPlatformPlugins() {
        this.pluginPlatform().getCandidates().values().stream().flatMap(Collection::stream)
            .filter(plugin -> PLATFORM_IDS.contains(plugin.metadata().id()))
            .map(plugin -> new VanillaDummyPluginContainer(plugin, this.logger(), this))
            .forEach(this.pluginManager()::addPlugin);
    }
}
