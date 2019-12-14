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
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.WorldType;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldInfo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.server.WorldManager;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Collection;
import java.util.UUID;

public interface SpongeWorldManager extends WorldManager {

    MinecraftServer getServer();

    boolean isDimensionTypeRegistered(DimensionType dimensionType);

    default UUID getDimensionTypeUniqueId(DimensionType dimensionType) {
        final WorldInfo info = this.getInfo(dimensionType);
        if (info == null) {
            return null;
        }

        return ((WorldProperties) info).getUniqueId();
    }

    @Nullable
    default ServerWorld getWorld(DimensionType dimensionType) {
        return this.getServer().getWorld(dimensionType);
    }

    @Nullable
    default ServerWorld getDefaultWorld() {
        return this.getWorld(DimensionType.OVERWORLD);
    }

    WorldInfo getInfo(DimensionType dimensionType);

    void loadAllWorlds(MinecraftServer server, String directoryName, String levelName, long seed, WorldType type, JsonElement generatorOptions);

    void adjustWorldForDifficulty(ServerWorld world, Difficulty newDifficulty, boolean isCustom);
}
