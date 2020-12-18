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

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.CommandStorage;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.SaveFormat;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.common.UntransformedAccessorError;
import org.spongepowered.common.UntransformedInvokerError;

import java.util.Map;
import java.util.concurrent.Executor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {

    @Accessor("LOGGER")
    static Logger accessor$LOGGER() {
        throw new UntransformedAccessorError();
    }

    @Accessor("fixerUpper") DataFixer accessor$fixerUpper();

    @Accessor("sessionService") MinecraftSessionService accessor$sessionService();

    @Accessor("profileRepository") GameProfileRepository accessor$profileRepository();

    @Accessor("profileCache") PlayerProfileCache accessor$profileCache();

    @Mutable @Accessor("profileCache") void accessor$profileCache(final PlayerProfileCache profileCache);

    @Accessor("commandStorage") void accessor$commandStorage(final CommandStorage commandStorage);

    @Accessor("storageSource") SaveFormat.LevelSave accessor$storageSource();

    @Accessor("levels") Map<RegistryKey<World>, ServerWorld> accessor$levels();

    @Accessor("resources") DataPackRegistries accessor$dataPackRegistries();

    @Accessor("executor") Executor accessor$executor();

    @Invoker("isSpawningMonsters") boolean invoker$isSpawningMonsters();

    @Invoker("setInitialSpawn") static void invoker$setInitialSpawn(ServerWorld serverWorld, IServerWorldInfo levelData, boolean generateBonusChest,
            boolean isDebugGeneration, boolean nonDebugSpawn) {
        throw new UntransformedInvokerError();
    }

    @Invoker("setupDebugLevel") void invoker$setDebugLevel(IServerConfiguration serverConfiguration);
}
