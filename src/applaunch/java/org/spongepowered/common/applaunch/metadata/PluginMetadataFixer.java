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
package org.spongepowered.common.applaunch.metadata;

import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.plugin.metadata.builtin.MetadataContainer;
import org.spongepowered.plugin.metadata.builtin.StandardPluginMetadata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PluginMetadataFixer {
    private static final Set<String> invalidPluginIds = new HashSet<>();

    public static MetadataContainer fixPluginIds(final MetadataContainer container) {
        boolean modified = false;
        final List<StandardPluginMetadata> metadata = new ArrayList<>();

        for (final PluginMetadata plugin : container.metadata()) {
            final String id = plugin.id();
            if (id.indexOf('-') >= 0) {
                final String newId = id.replace('-', '_');
                if (PluginMetadataFixer.invalidPluginIds.add(id)) {
                    AppLaunch.pluginPlatform().logger().warn("The dash character (-) is no longer supported in plugin ids.\n" +
                        "Plugin {} will be loaded as {}. If you are the developer of this plugin, please change the id.", id, newId);
                }

                final StandardPluginMetadata.Builder pluginBuilder = StandardPluginMetadata.builder()
                    .from((StandardPluginMetadata) plugin).id(newId).entrypoint(plugin.entrypoint());
                plugin.name().ifPresent(pluginBuilder::name);
                plugin.description().ifPresent(pluginBuilder::description);

                metadata.add(pluginBuilder.build());
                modified = true;
            } else {
                metadata.add((StandardPluginMetadata) plugin);
            }
        }

        if (!modified) {
            return container;
        }

        final MetadataContainer.Builder builder = container.toBuilder();
        builder.metadata(metadata);

        try {
            return builder.build();
        } catch (InvalidVersionSpecificationException e) {
            // This should not happen since we never modify the version.
            throw new RuntimeException(e);
        }
    }
}
