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

import com.mojang.serialization.DynamicOps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeBootstrap;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.client.MinecraftBridge;
import org.spongepowered.common.client.SpongeClient;
import org.spongepowered.common.datapack.SpongeDataPackManager;
import org.spongepowered.common.entity.player.ClientType;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.server.BootstrapProperties;

import java.nio.file.Path;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements MinecraftBridge, SpongeClient {

    // @formatter:off
    @Shadow private Thread gameThread;
    // @formatter:on

    private IntegratedServer impl$temporaryIntegratedServer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void impl$setClientOnGame(final GameConfig gameConfig, final CallbackInfo ci) {
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
        this.scheduler().tick();
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

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;shutdownExecutors()V"))
    private void impl$shutdownAsyncScheduler(final CallbackInfo ci) {
        SpongeCommon.getGame().asyncScheduler().close();
    }

    @Redirect(method = "loadWorldData", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/RegistryReadOps;create(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/RegistryAccess$RegistryHolder;)Lnet/minecraft/resources/RegistryReadOps;"))
    private static RegistryReadOps impl$setWorldSettingsAdapter(final DynamicOps p_244335_0_, final ResourceManager p_244335_1_, final RegistryAccess.RegistryHolder p_244335_2_) {
        final RegistryReadOps worldSettingsAdapter = RegistryReadOps.create(p_244335_0_, p_244335_1_, p_244335_2_);
        BootstrapProperties.worldSettingsAdapter(worldSettingsAdapter);
        return worldSettingsAdapter;
    }

    @Redirect(method = "loadWorldData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;getDataTag(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/world/level/DataPackConfig;)Lnet/minecraft/world/level/storage/WorldData;"))
    private static WorldData impl$setBootstrapProperties(final LevelStorageSource.LevelStorageAccess levelSave, final DynamicOps<Tag> p_237284_1_, final DataPackConfig p_237284_2_, final LevelStorageSource.LevelStorageAccess p_238181_0_, final RegistryAccess.RegistryHolder p_238181_1_) {
        final WorldData saveData = levelSave.getDataTag(p_237284_1_, p_237284_2_);
        BootstrapProperties.init(saveData.worldGenSettings(), saveData.getGameType(), saveData.getDifficulty(), true, saveData.isHardcore(),
            saveData.getAllowCommands(), 10, p_238181_1_);
        return saveData;
    }

    @Inject(method = "createLevel", at = @At("HEAD"))
    private void impl$setBootstrapProperties(String levelName, LevelSettings settings, RegistryAccess.RegistryHolder registries,
            WorldGenSettings dimensionGeneratorSettings, CallbackInfo ci) {
        BootstrapProperties.init(dimensionGeneratorSettings, settings.gameType(), settings.difficulty(), true, settings.hardcore(),
            settings.allowCommands(), 10, registries);
        BootstrapProperties.setIsNewLevel(true);
    }

    // TODO createLevel
//    @Inject(method = "*", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;encodeStart(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"))
//    private void impl$serializeDelayedDataPackOnCreate(final String param0, final LevelSettings param1, final RegistryAccess.RegistryHolder param2, WorldGenSettings param3,
//            final LevelStorageSource.LevelStorageAccess f1, final RegistryAccess.RegistryHolder f2, final ResourceManager f3, final DataPackConfig f4,
//            final CallbackInfoReturnable<WorldData> cir) {
//        SpongeDataPackManager.INSTANCE.serializeDelayedDataPack(DataPackTypes.WORLD);
//    }

    @Inject(method = "loadWorldData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;getDataTag(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/world/level/DataPackConfig;)Lnet/minecraft/world/level/storage/WorldData;"))
    private static void impl$serializeDelayedDataPackOnLoad(final LevelStorageSource.LevelStorageAccess param0, final RegistryAccess.RegistryHolder param1,
            ResourceManager param2, DataPackConfig param3, CallbackInfoReturnable<WorldData> cir) {
        SpongeDataPackManager.INSTANCE.serializeDelayedDataPack(DataPackTypes.WORLD);
    }

    @Redirect(method = "makeServerStem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;getLevelPath(Lnet/minecraft/world/level/storage/LevelResource;)Ljava/nio/file/Path;"))
    private Path impl$configurePackRepository(final LevelStorageSource.LevelStorageAccess levelSave, final LevelResource folderName) {
        final Path datapackDir = levelSave.getLevelPath(folderName);
        SpongeBootstrap.getLifecycle().callRegisterDataPackValueEvent(datapackDir);
        return datapackDir;
    }
}
