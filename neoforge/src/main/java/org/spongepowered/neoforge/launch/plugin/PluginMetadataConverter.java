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
package org.spongepowered.neoforge.launch.plugin;

import net.neoforged.fml.loading.moddiscovery.ModInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.plugin.metadata.builtin.StandardPluginMetadata;
import org.spongepowered.plugin.metadata.model.PluginContributor;
import org.spongepowered.plugin.metadata.model.PluginDependency;
import org.spongepowered.plugin.metadata.model.PluginLinks;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

public final class PluginMetadataConverter {

    public static PluginMetadata modToPlugin(final ModInfo info) {
        final StandardPluginMetadata.Builder builder = StandardPluginMetadata.builder();
        builder
            .id(info.getModId())
            .name(info.getDisplayName())
            .version(info.getVersion().toString())
            .description(info.getDescription())
            .entrypoint("unknown")
            .addContributor(new PluginContributor(info.getConfigElement("authors").orElse("unknown").toString(), Optional.empty()))
            .properties(info.getModProperties());

        try {
            final URL issueURL = info.getOwningFile().getIssueURL();
            builder.links(new PluginLinks(null, null, issueURL == null ? null : issueURL.toURI()));
        } catch (URISyntaxException ignored) {}

        for (final IModInfo.ModVersion dependency : info.getDependencies()) {
            builder.addDependency(new PluginDependency(
                dependency.getModId(),
                dependency.getVersionRange(),
                PluginMetadataConverter.orderingToLoad(dependency.getOrdering()),
                false
            ));
        }
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
