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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.server.integrated.IntegratedServer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeBootstrap;
import org.spongepowered.common.SpongeLifecycle;
import org.spongepowered.common.bridge.client.MinecraftBridge;
import org.spongepowered.common.applaunch.config.core.ConfigHandle;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.vanilla.client.VanillaClient;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin_Vanilla implements VanillaClient {

    @Shadow @Nullable private IntegratedServer integratedServer;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void vanilla$callStartedEngineAndLoadedGame(CallbackInfo ci) {
        // Save config now that registries have been initialized
        ConfigHandle.setSaveSuppressed(false);

        final SpongeLifecycle lifecycle = SpongeBootstrap.getLifecycle();
        lifecycle.callStartedEngineEvent(this);
        
        lifecycle.callLoadedGameEvent();
    }

    @Inject(method = "run", at = @At("HEAD"))
    private void vanilla$establishRegistriesAndStartingEngine(CallbackInfo ci) {
        final SpongeLifecycle lifecycle = SpongeBootstrap.getLifecycle();
        lifecycle.establishRegistries();
        lifecycle.establishDataProviders();

        // TODO Minecraft 1.14 - Evaluate exactly where we want to call this
        lifecycle.callStartingEngineEvent(this);
    }

    @Inject(method = "shutdownMinecraftApplet", at = @At("HEAD"))
    private void vanilla$callStoppingEngineEvent(CallbackInfo ci) {
        SpongeBootstrap.getLifecycle().callStoppingEngineEvent(this);
    }

    @Redirect(method = "unloadWorld(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;integratedServer:Lnet/minecraft/server/integrated/IntegratedServer;", opcode =
            Opcodes.PUTFIELD))
    private void vanilla$storeTemporaryServerRed(Minecraft minecraft, IntegratedServer server) {
        ((MinecraftBridge) minecraft).bridge$setTemporaryIntegratedServer(this.integratedServer);
        this.integratedServer = null;
    }

    @Inject(method = "unloadWorld(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("TAIL"))
    private void vanilla$nullServerRefAndPhaseTracker(Screen screenIn, CallbackInfo ci) {
        ((MinecraftBridge) this).bridge$setTemporaryIntegratedServer(null);
        try {
            PhaseTracker.SERVER.setThread(null);
        } catch (IllegalAccessException ignore) {
        }
    }
}
