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
package org.spongepowered.forge.applaunch.loading.metadata;

import net.minecraftforge.forgespi.language.IConfigurable;
import org.spongepowered.plugin.metadata.PluginDependency;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.plugin.metadata.PluginMetadataContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// ModFileInfo
public final class PluginFileConfigurable implements IConfigurable {

    private final PluginMetadataContainer container;

    public PluginFileConfigurable(final PluginMetadataContainer container) {
        this.container = container;
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

        if ("modLoader".equals(query)) {
            // TODO PluginMetadata will require moving this to per-file metadata and not per-plugin
            return (Optional<T>) Optional.of("sponge_java_plain");
        }

        if ("loaderVersion".equals(query)) {
            // TODO Need to have loaders "versioned" in plugin-spi or just always do 1.0...may be no point in this?
            return (Optional<T>) Optional.of("1.0");
        }

        if ("license".equals(query)) {
            // TODO PluginMetadata will require a "license" field
            return (Optional<T>) Optional.of("MIT");
        }

        if (key.length == 2) {
            final String plugin = key[1];
            final PluginMetadata metadata = this.container.metadata(plugin).orElse(null);
            if (metadata == null) {
                return Optional.empty();
            }

            if ("modproperties".equals(query)) {
                return (Optional<T>) Optional.of(metadata.extraMetadata());
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
            final Map<String, PluginMetadata> metadataById = this.container.allMetadata();
            if (metadataById.isEmpty()) {
                return Collections.emptyList();
            }

            final List<IConfigurable> metadataConfigurables = new ArrayList<>();
            metadataById.forEach((id, metadata) -> metadataConfigurables.add(new PluginMetadataConfigurable(metadata)));
            return metadataConfigurables;
        }

        if (key.length != 2) {
            return Collections.emptyList();
        }

        final String plugin = key[1];
        final PluginMetadata metadata = this.container.metadata(plugin).orElse(null);
        if (metadata == null) {
            return Collections.emptyList();
        }

        if ("dependencies".equals(query)) {

            // TODO Should we inject a dependency on SpongeForge?
            final List<PluginDependency> dependencies = metadata.dependencies();
            if (dependencies.isEmpty()) {
                return Collections.emptyList();
            }

            final List<IConfigurable> depConfigurables = new ArrayList<>();
            for (final PluginDependency dependency : dependencies) {
                depConfigurables.add(new PluginDependencyConfigurable(metadata, dependency));
            }

            return depConfigurables;
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
