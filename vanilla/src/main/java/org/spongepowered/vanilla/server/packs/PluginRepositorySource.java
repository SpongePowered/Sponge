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
package org.spongepowered.vanilla.server.packs;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.PluginResource;
import org.spongepowered.plugin.builtin.jvm.JVMPluginResource;
import org.spongepowered.vanilla.bridge.server.packs.repository.PackRepositoryBridge_Vanilla;
import org.spongepowered.vanilla.launch.plugin.VanillaPluginManager;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

public final class PluginRepositorySource implements RepositorySource {

    public static final PackSelectionConfig PLUGIN_SELECTION_CONFIG = new PackSelectionConfig(false, Pack.Position.BOTTOM, false);
    private final PackRepository repository;

    public PluginRepositorySource(final PackRepository repository) {
        this.repository = repository;
    }

    @Override
    public void loadPacks(final Consumer<Pack> callback) {
        final VanillaPluginManager pluginManager = (VanillaPluginManager) Launch.instance().pluginManager();

        // For each plugin, we create a pack. That pack might be empty.
        for (final PluginContainer pluginContainer : pluginManager.plugins()) {
            // The pack ID is prepended with "plugin-", as this will be the namespace we have to use a valid
            // character as a separator
            final String id = "plugin-" + pluginContainer.metadata().id();
            final PluginResource resource = pluginManager.resource(pluginContainer);
            // TODO: provide hook in the resource to return the file system for all resource types?
            @Nullable Path pluginRoot = null;
            if (resource instanceof JVMPluginResource jvmResource) {
                pluginRoot = jvmResource.resourcesRoot();
            }

            final PackLocationInfo info = new PackLocationInfo(id, Component.literal(id), PackSource.DEFAULT, Optional.empty());
            final PluginPackResources packResources = new PluginPackResources(info, pluginContainer, pluginRoot);
            final Pack.ResourcesSupplier packSupplier = new Pack.ResourcesSupplier() {

                @Override
                public PackResources openPrimary(final PackLocationInfo var1) {
                    return packResources;
                }

                @Override
                public PackResources openFull(final PackLocationInfo var1, final Pack.Metadata var2) {
                    return packResources;
                }
            };
            final Pack pack = Pack.readMetaAndCreate(info, packSupplier, PackType.SERVER_DATA, PLUGIN_SELECTION_CONFIG);
            ((PackRepositoryBridge_Vanilla) this.repository).bridge$registerResourcePack(pluginContainer, pack);
            callback.accept(pack);
        }
    }
}
