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
import net.minecraft.client.gui.screens.Screen;
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
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.applaunch.config.core.ConfigHandle;
import org.spongepowered.common.bridge.client.MinecraftBridge;
import org.spongepowered.common.client.SpongeClient;
import org.spongepowered.common.datapack.SpongeDataPackManager;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.launch.Lifecycle;
import org.spongepowered.common.server.BootstrapProperties;

import java.nio.file.Path;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements MinecraftBridge, SpongeClient {

    // @formatter:off
    @Shadow private Thread gameThread;
    @Shadow @Nullable private IntegratedServer singleplayerServer;
    // @formatter:on

    private IntegratedServer impl$temporaryIntegratedServer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void impl$setClientOnGame(final GameConfig gameConfig, final CallbackInfo ci) {
        SpongeCommon.game().setClient(this);
    }

    @Inject(method = "run", at = @At("HEAD"))
    private void impl$setThreadOnClientPhaseTracker(final CallbackInfo ci) {
        try {
            PhaseTracker.CLIENT.setThread(this.gameThread);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Could not initialize the client PhaseTracker!");
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$callStartedEngineAndLoadedGame(CallbackInfo ci) {
        // Save config now that registries have been initialized
        ConfigHandle.setSaveSuppressed(false);

        final Lifecycle lifecycle = Launch.instance().lifecycle();
        lifecycle.callStartedEngineEvent(this);

        lifecycle.callLoadedGameEvent();
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

    @Inject(method = "destroy", at = @At("HEAD"))
    private void impl$callStoppingEngineEvent(CallbackInfo ci) {
        Launch.instance().lifecycle().callStoppingEngineEvent(this);
    }

    @Redirect(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;singleplayerServer:Lnet/minecraft/client/server/IntegratedServer;", opcode =
            Opcodes.PUTFIELD))
    private void impl$storeTemporaryServerRef(Minecraft minecraft, IntegratedServer server) {
        ((MinecraftBridge) minecraft).bridge$setTemporaryIntegratedServer(this.singleplayerServer);
        this.singleplayerServer = null;
    }

    @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("TAIL"))
    private void impl$nullServerRefAndPhaseTracker(Screen screenIn, CallbackInfo ci) {
        ((MinecraftBridge) this).bridge$setTemporaryIntegratedServer(null);
        try {
            PhaseTracker.SERVER.setThread(null);
        } catch (IllegalAccessException ignore) {
        }
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;shutdownExecutors()V"))
    private void impl$callStoppedGame(final CallbackInfo ci) {
        Launch.instance().lifecycle().callStoppedGameEvent();
    }

    @Redirect(method = "loadWorldData", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/RegistryReadOps;create(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/RegistryAccess$RegistryHolder;)Lnet/minecraft/resources/RegistryReadOps;"))
    private static RegistryReadOps impl$setWorldSettingsAdapter(final DynamicOps p_244335_0_, final ResourceManager p_244335_1_, final RegistryAccess.RegistryHolder p_244335_2_) {
        final RegistryReadOps worldSettingsAdapter = RegistryReadOps.create(p_244335_0_, p_244335_1_, p_244335_2_);
        BootstrapProperties.worldSettingsAdapter(worldSettingsAdapter);
        return worldSettingsAdapter;
    }

    @Redirect(method = "loadWorldData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;getDataTag(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/world/level/DataPackConfig;)Lnet/minecraft/world/level/storage/WorldData;"))
    private static WorldData impl$serializeDelayedDataPackOnLoadAndSetBootstrapProperties(final LevelStorageSource.LevelStorageAccess levelSave, final DynamicOps<Tag> p_237284_1_, final DataPackConfig p_237284_2_, final LevelStorageSource.LevelStorageAccess p_238181_0_, final RegistryAccess.RegistryHolder p_238181_1_) {
        SpongeDataPackManager.INSTANCE.serializeDelayedDataPack(DataPackTypes.WORLD);
        final WorldData saveData = levelSave.getDataTag(p_237284_1_, p_237284_2_);
        BootstrapProperties.init(saveData.worldGenSettings(), saveData.getGameType(), saveData.getDifficulty(), true, saveData.isHardcore(),
                saveData.getAllowCommands(), 10, p_238181_1_);
        return saveData;
    }

    @Inject(method = "createLevel", at = @At("HEAD"))
    private void impl$setBootstrapProperties(final String levelName, final LevelSettings settings,
                                             final RegistryAccess.RegistryHolder registries,
                                             final WorldGenSettings dimensionGeneratorSettings,
                                             final CallbackInfo ci) {
        BootstrapProperties.init(dimensionGeneratorSettings, settings.gameType(), settings.difficulty(), true, settings.hardcore(),
            settings.allowCommands(), 10, registries);
        BootstrapProperties.setIsNewLevel(true);
    }

    @Redirect(method = "makeServerStem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;getLevelPath(Lnet/minecraft/world/level/storage/LevelResource;)Ljava/nio/file/Path;"))
    private Path impl$configurePackRepository(final LevelStorageSource.LevelStorageAccess levelSave, final LevelResource folderName) {
        final Path datapackDir = levelSave.getLevelPath(folderName);
        Launch.instance().lifecycle().callRegisterDataPackValueEvent(datapackDir);
        return datapackDir;
    }
}
