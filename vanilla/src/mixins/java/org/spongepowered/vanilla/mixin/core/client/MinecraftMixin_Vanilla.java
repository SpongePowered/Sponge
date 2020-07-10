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
package org.spongepowered.vanilla.mixin.core.client;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.Minecraft;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.listener.ChainedChunkStatusListener;
import net.minecraft.world.chunk.listener.IChunkStatusListenerFactory;
import net.minecraft.world.chunk.listener.TrackingChunkStatusListener;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.Engine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.accessor.server.MinecraftServerAccessor;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.launch.Launcher;
import org.spongepowered.vanilla.VanillaLifecycle;
import org.spongepowered.vanilla.launch.ClientLauncher;
import org.spongepowered.vanilla.launch.VanillaClient;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin_Vanilla {

    @Shadow @Nullable private IntegratedServer integratedServer;
    @Shadow @Final private Thread thread;
    @Shadow @Final private AtomicReference<TrackingChunkStatusListener> field_213277_ad;
    @Shadow @Final private Queue<Runnable> field_213275_aU;

    private VanillaLifecycle vanilla$lifeCycle;

    @Inject(method = "run", at = @At("HEAD"))
    private void vanilla$prepareGameAndLoadPlugins(CallbackInfo ci) {
        final ClientLauncher launcher = Launcher.getInstance();
        launcher.setupInjection((VanillaClient) this);
        launcher.loadPlugins((VanillaClient) this);

        try {
            PhaseTracker.CLIENT.setThread(this.thread);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not initialize the client PhaseTracker!");
        }

        this.vanilla$lifeCycle = new VanillaLifecycle((Engine) this);
        this.vanilla$lifeCycle.establishFactories();
        this.vanilla$lifeCycle.initTimings();
        this.vanilla$lifeCycle.registerPluginListeners();
        this.vanilla$lifeCycle.callConstructEvent();
        this.vanilla$lifeCycle.establishServices();

        // TODO Evaluate exactly where we want to call this
        this.vanilla$lifeCycle.callStartingEngineEvent();
    }

    @Inject(method = "launchIntegratedServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerProfileCache;<init>(Lcom/mojang/authlib/GameProfileRepository;Ljava/io/File;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void vanilla$createServerBeforeCache(String folderName, String worldName, WorldSettings worldSettingsIn, CallbackInfo ci,
        SaveHandler savehandler, WorldInfo worldinfo, YggdrasilAuthenticationService yggdrasilauthenticationservice, MinecraftSessionService minecraftsessionservice, GameProfileRepository gameprofilerepository) {
        this.integratedServer = new IntegratedServer((Minecraft) (Object) this, folderName, worldName, worldSettingsIn,
            yggdrasilauthenticationservice, minecraftsessionservice, gameprofilerepository, null, (p_213246_1_) -> {
            TrackingChunkStatusListener trackingchunkstatuslistener = new TrackingChunkStatusListener(p_213246_1_ + 0);
            trackingchunkstatuslistener.func_219521_a();
            this.field_213277_ad.set(trackingchunkstatuslistener);
            return new ChainedChunkStatusListener(trackingchunkstatuslistener, this.field_213275_aU::add);
        });
    }

    @Redirect(method = "launchIntegratedServer", at = @At(value = "NEW", target = "net/minecraft/server/integrated/IntegratedServer"))
    private IntegratedServer vanilla$provideAlreadyInitializedServer(Minecraft mcIn, String worldName, String p_i50895_3_,
        WorldSettings worldSettingsIn, YggdrasilAuthenticationService p_i50895_5_, MinecraftSessionService p_i50895_6_,
        GameProfileRepository p_i50895_7_, PlayerProfileCache p_i50895_8_, IChunkStatusListenerFactory p_i50895_9_) {
        ((MinecraftServerAccessor) this.integratedServer).accessor$setProfileCache(p_i50895_8_);
        return this.integratedServer;
    }
}
