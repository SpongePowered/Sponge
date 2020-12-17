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
package org.spongepowered.common.world.teleport;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.state.StateContainer;
import org.spongepowered.api.world.teleport.TeleportHelperFilter;
import org.spongepowered.common.applaunch.config.common.TeleportHelperCategory;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfigTeleportHelperFilter implements TeleportHelperFilter {

    // We try to cache this in case of big mod blacklists, we don't want to parse this
    // all the time.
    @Nullable private static List<BlockType> floorBlockTypes = null;
    @Nullable private static List<BlockState> floorBlockStates = null;
    @Nullable private static List<BlockType> bodyBlockTypes = null;
    @Nullable private static List<BlockState> bodyBlockStates = null;

    public static void invalidateCache() {
        ConfigTeleportHelperFilter.floorBlockTypes = null;
        ConfigTeleportHelperFilter.floorBlockStates = null;
        ConfigTeleportHelperFilter.bodyBlockStates = null;
        ConfigTeleportHelperFilter.bodyBlockTypes = null;
    }

    private static void updateCacheIfNecessary() {
        if (ConfigTeleportHelperFilter.floorBlockTypes == null) {
            final TeleportHelperCategory teleportHelperCat = SpongeConfigs.getCommon().get().teleportHelper;
            ConfigTeleportHelperFilter.floorBlockTypes = teleportHelperCat.unsafeFloorBlocks.stream()
                    .map(x -> ResourceKey.resolve(x.toLowerCase(Locale.ENGLISH)))
                    .map(x -> Sponge.getRegistry().getCatalogRegistry().get(BlockType.class, x).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            ConfigTeleportHelperFilter.floorBlockStates = teleportHelperCat.unsafeFloorBlocks.stream()
                    .map(x -> ResourceKey.resolve(x.toLowerCase(Locale.ENGLISH)))
                    .map(x -> Sponge.getRegistry().getCatalogRegistry().get(BlockType.class, x).map(StateContainer::getDefaultState).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            ConfigTeleportHelperFilter.bodyBlockTypes = teleportHelperCat.unsafeBlockBlocks.stream()
                    .map(x -> ResourceKey.resolve(x.toLowerCase(Locale.ENGLISH)))
                    .map(x -> Sponge.getRegistry().getCatalogRegistry().get(BlockType.class, x).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            ConfigTeleportHelperFilter.bodyBlockStates = teleportHelperCat.unsafeBlockBlocks.stream()
                    .map(x -> ResourceKey.resolve(x.toLowerCase(Locale.ENGLISH)))
                    .map(x -> Sponge.getRegistry().getCatalogRegistry().get(BlockType.class, x).map(StateContainer::getDefaultState).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    private final ResourceKey key;

    public ConfigTeleportHelperFilter() {
        this.key = ResourceKey.sponge("config");
    }

    @Override
    @NonNull
    public ResourceKey getKey() {
        return this.key;
    }

    @Override
    public boolean isSafeFloorMaterial(@NonNull final BlockState blockState) {
        ConfigTeleportHelperFilter.updateCacheIfNecessary();
        return !ConfigTeleportHelperFilter.floorBlockStates.contains(blockState) && !ConfigTeleportHelperFilter.floorBlockTypes.contains(blockState.getType());
    }

    @Override
    public boolean isSafeBodyMaterial(@NonNull final BlockState blockState) {
        ConfigTeleportHelperFilter.updateCacheIfNecessary();
        return !ConfigTeleportHelperFilter.bodyBlockStates.contains(blockState) && !ConfigTeleportHelperFilter.bodyBlockTypes.contains(blockState.getType());
    }
}
