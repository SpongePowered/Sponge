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
package org.spongepowered.common.mixin.core.client;

import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.DynamicOps;
import io.netty.util.internal.shaded.org.jctools.queues.atomic.LinkedQueueAtomicNode;
import net.minecraft.client.GameConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.INBT;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.datafix.codec.DatapackCodec;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.WorldSettingsImport;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.storage.FolderName;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.SaveFormat;
import org.checkerframework.checker.units.qual.Area;
import org.checkerframework.common.value.qual.IntRangeFromGTENegativeOne;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeBootstrap;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.client.MinecraftBridge;
import org.spongepowered.common.client.SpongeClient;
import org.spongepowered.common.entity.player.ClientType;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.server.BootstrapProperties;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements MinecraftBridge, SpongeClient {

    // @formatter:off
    @Shadow private Thread gameThread;

    @Shadow public abstract Minecraft.PackManager shadow$makeServerStem(DynamicRegistries.Impl p_238189_1_,
            Function<SaveFormat.LevelSave, DatapackCodec> p_238189_2_,
            Function4<SaveFormat.LevelSave, DynamicRegistries.Impl, IResourceManager, DatapackCodec, IServerConfiguration> p_238189_3_,
            boolean p_238189_4_, SaveFormat.LevelSave p_238189_5_) throws InterruptedException, ExecutionException;
    // @formatter:on

    private IntegratedServer impl$temporaryIntegratedServer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void impl$setClientOnGame(final GameConfiguration gameConfig, final CallbackInfo ci) {
        SpongeCommon.getGame().setClient(this);
    }

    @Inject(method = "run", at = @At("HEAD"))
    private void impl$setThreadOnClientPhaseTracker(final CallbackInfo ci) {
        try {
            PhaseTracker.CLIENT.setThread(this.gameThread);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Could not initialize the client PhaseTracker!");
        }
    }

    @Inject(method = "runTick", at = @At("TAIL"))
    private void impl$tickClientScheduler(final boolean renderWorldIn, final CallbackInfo ci) {
        this.getScheduler().tick();
    }

    @Override
    public IntegratedServer bridge$getTemporaryIntegratedServer() {
        return this.impl$temporaryIntegratedServer;
    }

    @Override
    public void bridge$setTemporaryIntegratedServer(final IntegratedServer server) {
        this.impl$temporaryIntegratedServer = server;
    }

    @Override
    public ClientType bridge$getClientType() {
        return ClientType.SPONGE_VANILLA;
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;shutdownExecutors()V"))
    private void impl$shutdownAsyncScheduler(final CallbackInfo ci) {
        SpongeCommon.getGame().getAsyncScheduler().close();
    }

    @Redirect(method = "loadWorldData", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/WorldSettingsImport;create(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/resources/IResourceManager;Lnet/minecraft/util/registry/DynamicRegistries$Impl;)Lnet/minecraft/util/registry/WorldSettingsImport;"))
    private static WorldSettingsImport impl$setWorldSettingsAdapter(final DynamicOps p_244335_0_, final IResourceManager p_244335_1_, final DynamicRegistries.Impl p_244335_2_) {
        final WorldSettingsImport worldSettingsAdapter = WorldSettingsImport.create(p_244335_0_, p_244335_1_, p_244335_2_);
        BootstrapProperties.worldSettingsAdapter(worldSettingsAdapter);
        return worldSettingsAdapter;
    }

    @Redirect(method = "loadWorldData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/SaveFormat$LevelSave;getDataTag(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/util/datafix/codec/DatapackCodec;)Lnet/minecraft/world/storage/IServerConfiguration;"))
    private static IServerConfiguration impl$setBootstrapProperties(final SaveFormat.LevelSave levelSave, final DynamicOps<INBT> p_237284_1_, final DatapackCodec p_237284_2_, final SaveFormat.LevelSave p_238181_0_, final DynamicRegistries.Impl p_238181_1_) {
        final IServerConfiguration saveData = levelSave.getDataTag(p_237284_1_, p_237284_2_);
        BootstrapProperties.init(saveData.worldGenSettings(), saveData.getGameType(), saveData.getDifficulty(), true, saveData.isHardcore(), 10, p_238181_1_);
        return saveData;
    }

    @Inject(method = "createLevel", at = @At("HEAD"))
    private void impl$setBootstrapProperties(String levelName, WorldSettings settings, DynamicRegistries.Impl registries,
            DimensionGeneratorSettings dimensionGeneratorSettings, CallbackInfo ci) {
        BootstrapProperties.init(dimensionGeneratorSettings, settings.gameType(), settings.difficulty(), true, settings.hardcore(), 10, registries);
        BootstrapProperties.setIsNewLevel(true);
    }

    @Redirect(method = "makeServerStem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/SaveFormat$LevelSave;getLevelPath(Lnet/minecraft/world/storage/FolderName;)Ljava/nio/file/Path;"))
    private Path impl$configurePackRepository(final SaveFormat.LevelSave levelSave, final FolderName folderName) {
        final Path datapackDir = levelSave.getLevelPath(folderName);
        SpongeBootstrap.getLifecycle().callRegisterDataPackValueEvent(datapackDir);
        return datapackDir;
    }
}
