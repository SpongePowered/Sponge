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

import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.plugin.metadata.builtin.MetadataContainer;
import org.spongepowered.plugin.metadata.builtin.StandardPluginMetadata;
import org.spongepowered.plugin.metadata.builtin.model.StandardPluginContributor;
import org.spongepowered.plugin.metadata.builtin.model.StandardPluginDependency;
import org.spongepowered.plugin.metadata.builtin.model.StandardPluginLinks;
import org.spongepowered.plugin.metadata.model.PluginDependency;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PluginMetadataUtils {
    private static final Set<String> invalidPluginIds = new HashSet<>();

    public static MetadataContainer fixPluginIds(final MetadataContainer container) {
        boolean modified = false;
        final List<StandardPluginMetadata> metadata = new ArrayList<>();

        for (final PluginMetadata plugin : container.metadata()) {
            final String id = plugin.id();
            if (id.indexOf('-') >= 0) {
                final String newId = id.replace('-', '_');
                if (PluginMetadataUtils.invalidPluginIds.add(id)) {
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

    public static PluginMetadata modToPlugin(final ModInfo info) {
        final StandardPluginMetadata.Builder builder = StandardPluginMetadata.builder();
        builder
                .id(info.getModId())
                .name(info.getDisplayName())
                .version(info.getVersion().toString())
                .description(info.getDescription())
                .entrypoint("unknown") // TODO Map main class to mod id
                .addContributor(StandardPluginContributor.builder()
                        .name(info.getConfigElement("authors").orElse("unknown").toString())
                        .build());
        builder.links(StandardPluginLinks.builder().issues(info.getOwningFile().getIssueURL()).build());

        final List<StandardPluginDependency> dependencies = new ArrayList<>();
        for (final IModInfo.ModVersion dependency : info.getDependencies()) {
            final StandardPluginDependency.Builder depBuilder = StandardPluginDependency.builder();
            depBuilder
                    .id(dependency.getModId())
                    .loadOrder(PluginMetadataUtils.orderingToLoad(dependency.getOrdering()))
                    .version(dependency.getVersionRange().toString())
            ;

            dependencies.add(depBuilder.build());
        }

        if (!dependencies.isEmpty()) {
            builder.dependencies(dependencies);
        }

        return builder.build();
    }

    private static PluginDependency.LoadOrder orderingToLoad(final IModInfo.Ordering ordering) {
        if (ordering == IModInfo.Ordering.AFTER) {
            return PluginDependency.LoadOrder.AFTER;
        }
        return PluginDependency.LoadOrder.UNDEFINED;
    }
}
