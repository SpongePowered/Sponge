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
package org.spongepowered.server.accessor.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.chunk.listener.IChunkStatusListenerFactory;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.File;
import java.util.Map;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor_Vanilla {

    @Accessor("LOGGER") static Logger accessor$getLogger() {
        throw new RuntimeException("Accessor was not mixed!");
    }

    @Accessor("anvilFile") File accessor$getAnvilFile();

    @Accessor("chunkStatusListenerFactory") IChunkStatusListenerFactory accessor$getChunkStatusListenerFactory();

    @Accessor("enableBonusChest") boolean accessor$getEnableBonusChest();

    @Accessor("worlds") Map<DimensionType, ServerWorld> accessor$getWorlds();

    @Accessor("serverTime") void accessor$setServerTime(long serverTime);

    @Invoker("convertMapIfNeeded") void accessor$convertMapIfNeeded(String worldName);

    @Invoker("setUserMessage") void accessor$setUserMessage(ITextComponent userMessage);

    @Invoker("setResourcePackFromWorld") void accessor$setResourcePackFromWorld(String worldName, SaveHandler saveHandler);

    @Invoker("loadDataPacks") void accessor$loadDataPacks(File directory, WorldInfo worldInfo);

    @Invoker("applyDebugWorldInfo") void accessor$applyDebugWorldInfo(WorldInfo worldInfo);

    @Invoker("func_213204_a") void accessor$func_213204_a(DimensionSavedDataManager manager);

    @Invoker("stopServer") void accessor$stopServer();

    @Invoker("runScheduledTasks") void accessor$runScheduledTasks();
}
