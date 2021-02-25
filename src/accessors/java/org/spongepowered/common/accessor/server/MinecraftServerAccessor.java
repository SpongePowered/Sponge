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
package org.spongepowered.common.accessor.server;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.CommandStorage;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.common.UntransformedAccessorError;
import org.spongepowered.common.UntransformedInvokerError;

import java.util.Map;
import java.util.concurrent.Executor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {

    @Accessor("LOGGER") static Logger accessor$LOGGER() {
        throw new UntransformedAccessorError();
    }

    @Invoker("setInitialSpawn") static void invoker$setInitialSpawn(final ServerLevel serverWorld, final ServerLevelData levelData, final boolean generateBonusChest,
            final boolean isDebugGeneration, final boolean nonDebugSpawn) {
        throw new UntransformedInvokerError();
    }

    @Accessor("commandStorage") void accessor$commandStorage(final CommandStorage commandStorage);

    @Accessor("storageSource") LevelStorageSource.LevelStorageAccess accessor$storageSource();

    @Accessor("levels") Map<ResourceKey<Level>, ServerLevel> accessor$levels();

    @Accessor("executor") Executor accessor$executor();

    @Accessor("progressListenerFactory") ChunkProgressListenerFactory accessor$getProgressListenerFactory();

    @Accessor("nextTickTime") void accessor$setNextTickTime(long nextTickTime);

    @Invoker("isSpawningMonsters") boolean invoker$isSpawningMonsters();

    @Invoker("setupDebugLevel") void invoker$setDebugLevel(WorldData serverConfiguration);

    @Invoker("forceDifficulty") void invoker$forceDifficulty();

    @Invoker("readScoreboard") void accessor$readScoreboard(DimensionDataStorage manager);

    @Invoker("waitUntilNextTick") void accessor$waitUntilNextTick();
}
