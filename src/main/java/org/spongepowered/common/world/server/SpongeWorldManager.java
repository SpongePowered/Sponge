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

import com.google.gson.JsonElement;
import net.minecraft.world.Difficulty;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldInfo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.server.WorldManager;
import org.spongepowered.api.world.storage.WorldProperties;

import java.nio.file.Path;
import java.util.UUID;

public interface SpongeWorldManager extends WorldManager {

    ResourceKey VANILLA_OVERWORLD = ResourceKey.minecraft("overworld");
    ResourceKey VANILLA_THE_NETHER = ResourceKey.minecraft("the_nether");
    ResourceKey VANILLA_THE_END = ResourceKey.minecraft("the_end");

    Path getSavesDirectory();

    boolean registerPendingWorld(ResourceKey key, WorldArchetype archetype);

    @Nullable
    ServerWorld getWorld(final DimensionType dimensionType);

    @Nullable
    ServerWorld getWorld0(final ResourceKey key);

    @Nullable
    ServerWorld getDefaultWorld();

    void loadAllWorlds(String directoryName, String levelName, long seed, WorldType type, JsonElement generatorOptions, boolean isSinglePlayer, @Nullable
            WorldSettings defaultSettings, Difficulty defaultDifficulty);

    void adjustWorldForDifficulty(ServerWorld world, Difficulty newDifficulty, boolean isCustom);

    default String getDirectoryName(final ResourceKey key) {
        if (SpongeWorldManager.VANILLA_OVERWORLD.equals(key)) {
            return "";
        }
        if (SpongeWorldManager.VANILLA_THE_NETHER.equals(key)) {
            return "DIM-1";
        }
        if (SpongeWorldManager.VANILLA_THE_END.equals(key)) {
            return "DIM1";
        }
        return key.getValue();
    }
}
