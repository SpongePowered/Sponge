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

import net.minecraft.client.GameConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.client.MinecraftBridge;
import org.spongepowered.common.client.SpongeClient;
import org.spongepowered.common.entity.player.ClientType;
import org.spongepowered.common.event.tracking.PhaseTracker;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements MinecraftBridge, SpongeClient {

    // @formatter:off
    @Shadow private Thread gameThread;
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
    private void impl$tickClientScheduler(boolean renderWorldIn, CallbackInfo ci) {
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
}
