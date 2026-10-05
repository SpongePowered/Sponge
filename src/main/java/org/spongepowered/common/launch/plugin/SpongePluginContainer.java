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
package org.spongepowered.common.launch.plugin;

import com.google.inject.Injector;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.inject.plugin.PluginGuice;
import org.spongepowered.plugin.builtin.StandardPluginContainer;
import org.spongepowered.plugin.discovery.PluginResource;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.plugin.metadata.model.PluginEntrypoints;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("deprecation") // registerListeners
public class SpongePluginContainer extends StandardPluginContainer {
    private Object instance = this;
    private Injector injector;

    public SpongePluginContainer(final PluginResource resource, final PluginMetadata metadata) {
        super(resource, metadata);
    }

    @SuppressWarnings("removal")
    @Override
    public Object instance() {
        return this.instance;
    }

    public Optional<Injector> injector() {
        return Optional.ofNullable(this.injector);
    }

    public void loadMainEntrypoints() throws Exception {
        final List<Class<?>> pluginClasses = new ArrayList<>();
        for (final String className : this.metadata().entrypoints().main()) {
            pluginClasses.add(Class.forName(className));
        }

        final Injector pluginInjector = PluginGuice.createMain(this, pluginClasses);
        this.injector = pluginInjector;

        for (final Class<?> pluginClass : pluginClasses) {
            final Object plugin = pluginInjector.getInstance(pluginClass);
            if (this.instance == this) {
                this.instance = plugin;
            }
            Sponge.eventManager().registerListeners(this, plugin);
        }
    }

    public void loadSidedEntrypoints(boolean server) throws Exception {
        final List<Class<?>> pluginClasses = new ArrayList<>();
        final PluginEntrypoints entrypoints = this.metadata().entrypoints();
        for (final String className : server ? entrypoints.server() : entrypoints.client()) {
            pluginClasses.add(Class.forName(className));
        }

        final Injector pluginInjector = PluginGuice.createChild(this, pluginClasses);
        for (final Class<?> pluginClass : pluginClasses) {
            final Object plugin = pluginInjector.getInstance(pluginClass);
            Sponge.eventManager().registerListeners(this, plugin);
        }
    }
}
