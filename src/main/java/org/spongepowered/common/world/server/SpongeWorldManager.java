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
package org.spongepowered.common.world.server;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.api.world.server.WorldManager;
import org.spongepowered.common.SpongeCommon;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public interface SpongeWorldManager extends WorldManager {

    Path getDefaultWorldDirectory();

    Path getCustomWorldsDirectory();

    static RegistryKey<World> createRegistryKey(final ResourceKey key) {
        return RegistryKey.create(Registry.DIMENSION_REGISTRY, (ResourceLocation) (Object) key);
    }

    void unloadWorld0(final ServerWorld world) throws IOException;

    @Nullable
    ServerWorld getDefaultWorld();

    void adjustWorldForDifficulty(ServerWorld world, Difficulty newDifficulty, boolean forceDifficulty);

    void loadLevel();

    default String getDirectoryName(final ResourceKey key) {
        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(key);
        if (World.OVERWORLD.equals(registryKey)) {
            return "";
        }
        if (World.NETHER.equals(registryKey)) {
            return "DIM-1";
        }
        if (World.END.equals(registryKey)) {
            return "DIM1";
        }
        return key.getValue();
    }

    default boolean isVanillaWorld(final ResourceKey key) {
        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(key);
        return World.OVERWORLD.equals(registryKey) || World.NETHER.equals(registryKey) || World.END.equals(registryKey);
    }

    default boolean isVanillaSubWorld(final String directoryName) {
        return "DIM-1".equals(directoryName) || "DIM1".equals(directoryName);
    }

    default boolean isDefaultWorld(final ResourceKey key) {
        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(key);
        return World.OVERWORLD.equals(registryKey);
    }

    default DimensionType getVanillaDimensionType(final ResourceKey key) {
        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(key);

        if (World.OVERWORLD.equals(registryKey)) {
            return SpongeCommon.getServer().registryAccess().dimensionTypes().get(DimensionType.OVERWORLD_LOCATION);
        } else if (World.NETHER.equals(registryKey)) {
            return SpongeCommon.getServer().registryAccess().dimensionTypes().get(DimensionType.NETHER_LOCATION);
        } else if (World.END.equals(registryKey)) {
            return SpongeCommon.getServer().registryAccess().dimensionTypes().get(DimensionType.END_LOCATION);
        }

        throw new RuntimeException(String.format("Should be impossible, a non-Vanilla fresh world '%s' was hit!", key));
    }

    default boolean isDefaultWorld(final String directoryName) {
        return SpongeCommon.getServer().getWorldData().getLevelName().equals(directoryName);
    }
}
