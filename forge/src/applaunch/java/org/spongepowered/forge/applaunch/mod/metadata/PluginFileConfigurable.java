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
package org.spongepowered.forge.applaunch.mod.metadata;

import net.minecraftforge.forgespi.language.IConfigurable;
import org.spongepowered.plugin.discovery.PluginResource;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.util.*;
import java.util.stream.Collectors;

// ModFileInfo
public final class PluginFileConfigurable implements IConfigurable {
    private final Map<String, PluginMetadata> plugins = new LinkedHashMap<>();
    private final PluginResource resource;

    public PluginFileConfigurable(final PluginResource resource, final Collection<PluginMetadata> plugins) {
        for (final PluginMetadata plugin : plugins) {
            this.plugins.put(plugin.id(), plugin);
        }
        this.resource = resource;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> getConfigElement(final String... key) {
        if (key.length < 1) {
            return Optional.empty();
        }

        final String query = key[0];
        if (key.length != this.requiredConfigElements(query)) {
            return Optional.empty();
        }

        if ("spongeResource".equals(query)) {
            return Optional.of((T) this.resource);
        }

        if ("modLoader".equals(query)) {
            return Optional.of((T) "sponge");
        }

        if ("loaderVersion".equals(query)) {
            return Optional.of((T) "*");
        }

        if ("license".equals(query)) {
            final String joinedLicenses = this.plugins.values().stream()
                .flatMap(m -> m.license().stream()).distinct().collect(Collectors.joining(", "));
            return Optional.of((T) (joinedLicenses.isEmpty() ? "?" : joinedLicenses));
        }

        if (key.length == 2) {
            final String plugin = key[1];
            final PluginMetadata metadata = this.plugins.get(plugin);
            if (metadata == null) {
                return Optional.empty();
            }

            if ("modproperties".equals(query)) {
                return Optional.of((T) metadata.properties());
            }
        }

        return Optional.empty();
    }

    @Override
    public List<? extends IConfigurable> getConfigList(final String... key) {
        if (key.length < 1) {
            return Collections.emptyList();
        }

        final String query = key[0];
        if (key.length != this.requiredConfigElements(query)) {
            return Collections.emptyList();
        }

        if ("mods".equals(query)) {
            return this.plugins.values().stream().map(PluginMetadataConfigurable::new).toList();
        }

        if (key.length != 2) {
            return Collections.emptyList();
        }

        final String pluginId = key[1];
        final PluginMetadata metadata = this.plugins.get(pluginId);
        if (metadata == null) {
            return Collections.emptyList();
        }

        if ("dependencies".equals(query)) {
            return metadata.dependencies().stream().map(PluginDependencyConfigurable::new).toList();
        }

        return Collections.emptyList();
    }

    private int requiredConfigElements(final String query) {
        if ("dependencies".equals(query) || "modproperties".equals(query)) {
            return 2;
        }
        return 1;
    }
}
