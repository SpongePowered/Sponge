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
import org.spongepowered.plugin.metadata.PluginContributor;
import org.spongepowered.plugin.metadata.PluginDependency;
import org.spongepowered.plugin.metadata.PluginLinks;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.util.ArrayList;
import java.util.List;

/**
 * ModInfo cannot be mixed into as it is on the app class loader so this somewhat hacky util class will do...
 */
public final class PluginMetadataUtils {

    public static PluginMetadata modToPlugin(final ModInfo info) {
        final PluginMetadata.Builder builder = PluginMetadata.builder();
        builder
                .loader(info.getOwningFile().getModLoader())
                .id(info.getModId())
                .name(info.getDisplayName())
                .version(info.getVersion().toString())
                .description(info.getDescription())
                .mainClass("unknown") // TODO Map main class to mod id
                .addContributor(PluginContributor.builder().name(info.getConfigElement("authors").orElse("unknown").toString())
                        .build());
        builder.links(PluginLinks.builder().issues(info.getOwningFile().getIssueURL()).build());

        final List<PluginDependency> dependencies = new ArrayList<>();
        for (final IModInfo.ModVersion dependency : info.getDependencies()) {
            final PluginDependency.Builder depBuilder = PluginDependency.builder();
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
