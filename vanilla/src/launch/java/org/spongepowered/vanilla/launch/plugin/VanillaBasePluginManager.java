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

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.spongepowered.common.launch.plugin.SpongePluginManager;
import org.spongepowered.plugin.PluginContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class VanillaBasePluginManager implements SpongePluginManager {
    protected final Map<String, PluginContainer> plugins;
    protected final Map<Object, PluginContainer> instancesToPlugins;
    protected final List<PluginContainer> sortedPlugins;

    public VanillaBasePluginManager() {
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

    public void addPlugin(final PluginContainer plugin) {
        this.plugins.put(plugin.metadata().id(), Objects.requireNonNull(plugin, "plugin"));
        this.sortedPlugins.add(plugin);

        if (!(plugin instanceof VanillaDummyPluginContainer)) {
            this.instancesToPlugins.put(plugin.instance(), plugin);
        }
    }
}
