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
package org.spongepowered.forge.launch.plugin;

import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.plugin.metadata.builtin.StandardPluginMetadata;
import org.spongepowered.plugin.metadata.builtin.model.StandardPluginContributor;
import org.spongepowered.plugin.metadata.builtin.model.StandardPluginDependency;
import org.spongepowered.plugin.metadata.builtin.model.StandardPluginLinks;
import org.spongepowered.plugin.metadata.model.PluginDependency;

import java.util.ArrayList;
import java.util.List;

public final class PluginMetadataConverter {

    @SuppressWarnings("UnstableApiUsage")
    public static PluginMetadata modToPlugin(final ModInfo info) {
        final StandardPluginMetadata.Builder builder = StandardPluginMetadata.builder();
        builder
            .id(info.getModId())
            .name(info.getDisplayName())
            .version(info.getVersion().toString())
            .description(info.getDescription())
            .entrypoint("unknown")
            .addContributor(StandardPluginContributor.builder().name(info.getConfigElement("authors").orElse("unknown").toString()).build())
            .links(StandardPluginLinks.builder().issues(info.getOwningFile().getIssueURL()).build());

        final List<StandardPluginDependency> dependencies = new ArrayList<>();
        for (final IModInfo.ModVersion dependency : info.getDependencies()) {
            final StandardPluginDependency.Builder depBuilder = StandardPluginDependency.builder();
            depBuilder
                .id(dependency.getModId())
                .loadOrder(PluginMetadataConverter.orderingToLoad(dependency.getOrdering()))
                .version(dependency.getVersionRange().toString());

            dependencies.add(depBuilder.build());
        }

        builder.dependencies(dependencies);
        return builder.build();
    }

    private static PluginDependency.LoadOrder orderingToLoad(final IModInfo.Ordering ordering) {
        return switch (ordering) {
            case BEFORE -> PluginDependency.LoadOrder.BEFORE;
            case AFTER -> PluginDependency.LoadOrder.AFTER;
            case NONE -> PluginDependency.LoadOrder.UNDEFINED;
        };
    }
}
