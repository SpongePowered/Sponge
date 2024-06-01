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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.Opcodes;
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
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.launch.Lifecycle;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin extends ReentrantBlockableEventLoop<Runnable> implements MinecraftBridge, SpongeClient {

    // @formatter:off
    @Shadow private Thread gameThread;
    @Shadow @Nullable private IntegratedServer singleplayerServer;
    // @formatter:on

    private IntegratedServer impl$temporaryIntegratedServer;

    public MinecraftMixin(String param0) {
        super(param0);
    }

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

    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;runAllTasks()V"))
    private void impl$tickClientScheduler(final Minecraft minecraft) {
        this.scheduler().tick();
        this.runAllTasks();
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

    @Redirect(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;singleplayerServer:Lnet/minecraft/client/server/IntegratedServer;", opcode =
            Opcodes.PUTFIELD))
    private void impl$storeTemporaryServerRef(Minecraft minecraft, IntegratedServer server) {
        ((MinecraftBridge) minecraft).bridge$setTemporaryIntegratedServer(this.singleplayerServer);
        this.singleplayerServer = null;
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("TAIL"))
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

}
