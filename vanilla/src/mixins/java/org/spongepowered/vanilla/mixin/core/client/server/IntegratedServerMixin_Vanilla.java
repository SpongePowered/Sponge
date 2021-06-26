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
package org.spongepowered.vanilla.mixin.core.client.server;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ServerResources;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeBootstrap;
import org.spongepowered.common.SpongeLifecycle;
import org.spongepowered.common.bridge.client.MinecraftBridge;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;
import org.spongepowered.vanilla.VanillaServer;
import org.spongepowered.vanilla.mixin.core.server.MinecraftServerMixin_Vanilla;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin_Vanilla extends MinecraftServerMixin_Vanilla implements MinecraftServerBridge, VanillaServer  {

    // @formatter:off
    @Shadow private boolean paused;
    // @formatter:on

    @Inject(method = "<init>", at = @At("RETURN"))
    private void vanilla$setIntegratedServer(Thread p_i232494_1_, Minecraft p_i232494_2_, RegistryAccess.RegistryHolder p_i232494_3_,
            LevelStorageSource.LevelStorageAccess p_i232494_4_, PackRepository p_i232494_5_, ServerResources p_i232494_6_, WorldData p_i232494_7_,
            MinecraftSessionService p_i232494_8_, GameProfileRepository p_i232494_9_, GameProfileCache p_i232494_10_,
            ChunkProgressListenerFactory p_i232494_11_, CallbackInfo ci) {
        ((MinecraftBridge) p_i232494_2_).bridge$setTemporaryIntegratedServer((IntegratedServer) (Object) this);
    }

    @Inject(method = "initServer", at = @At("HEAD"))
    private void vanilla$runEngineStartLifecycle(final CallbackInfoReturnable<Boolean> cir) {
        final SpongeLifecycle lifecycle = SpongeBootstrap.lifecycle();
        lifecycle.establishServerServices();

        lifecycle.establishServerFeatures();

        lifecycle.establishServerRegistries(this);
        lifecycle.callStartingEngineEvent(this);
    }

    @Inject(method = "initServer", at = @At("RETURN"))
    private void vanilla$callStartedEngineAndLoadedGame(final CallbackInfoReturnable<Boolean> cir) {
        final SpongeLifecycle lifecycle = SpongeBootstrap.lifecycle();
        lifecycle.callStartedEngineEvent(this);
    }

    @Override
    public void loadLevel() {
        this.shadow$detectBundledResources();
        this.worldManager().loadLevel();
    }

    @Override
    public boolean bridge$performAutosaveChecks() {
        if (!this.shadow$isRunning()) {
            return false;
        }

        return !this.paused;
    }
}
