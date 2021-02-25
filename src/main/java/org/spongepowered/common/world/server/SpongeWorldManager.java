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

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
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

    Path getDimensionDataPackDirectory();

    static net.minecraft.resources.ResourceKey<Level> createRegistryKey(final ResourceKey key) {
        return net.minecraft.resources.ResourceKey.create(Registry.DIMENSION_REGISTRY, (ResourceLocation) (Object) key);
    }

    void unloadWorld0(final ServerLevel world) throws IOException;

    void loadLevel();

    default String getDirectoryName(final ResourceKey key) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(key);
        if (Level.OVERWORLD.equals(registryKey)) {
            return "";
        }
        if (Level.NETHER.equals(registryKey)) {
            return "DIM-1";
        }
        if (Level.END.equals(registryKey)) {
            return "DIM1";
        }
        return key.getValue();
    }

    default boolean isVanillaWorld(final ResourceKey key) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(key);
        return Level.OVERWORLD.equals(registryKey) || Level.NETHER.equals(registryKey) || Level.END.equals(registryKey);
    }

    default boolean isVanillaSubWorld(final String directoryName) {
        return "DIM-1".equals(directoryName) || "DIM1".equals(directoryName);
    }

    default boolean isDefaultWorld(final ResourceKey key) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(key);
        return Level.OVERWORLD.equals(registryKey);
    }

    default Path getDataPackFile(final ResourceKey key) {
        return this.getDimensionDataPackDirectory().resolve(key.getNamespace()).resolve("dimension").resolve(key.getValue() + ".json");
    }
}
