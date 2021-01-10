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

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.chunk.listener.IChunkStatusListenerFactory;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.CommandStorage;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.SaveFormat;
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

    @Invoker("setInitialSpawn") static void invoker$setInitialSpawn(final ServerWorld serverWorld, final IServerWorldInfo levelData, final boolean generateBonusChest,
            final boolean isDebugGeneration, final boolean nonDebugSpawn) {
        throw new UntransformedInvokerError();
    }

    @Accessor("commandStorage") void accessor$commandStorage(final CommandStorage commandStorage);

    @Accessor("storageSource") SaveFormat.LevelSave accessor$storageSource();

    @Accessor("levels") Map<RegistryKey<World>, ServerWorld> accessor$levels();

    @Accessor("executor") Executor accessor$executor();

    @Accessor("progressListenerFactory") IChunkStatusListenerFactory accessor$getProgressListenerFactory();

    @Accessor("levels") Map<RegistryKey<World>, ServerWorld> accessor$getLevels();

    @Accessor("nextTickTime") void accessor$setNextTickTime(long nextTickTime);

    @Invoker("isSpawningMonsters") boolean invoker$isSpawningMonsters();

    @Invoker("setupDebugLevel") void invoker$setDebugLevel(IServerConfiguration serverConfiguration);

    @Invoker("forceDifficulty") void invoker$forceDifficulty();

    @Invoker("detectBundledResources") void accessor$detectBundledResources();

    @Invoker("readScoreboard") void accessor$readScoreboard(DimensionSavedDataManager manager);

    @Invoker("waitUntilNextTick") void accessor$waitUntilNextTick();
}
