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
package org.spongepowered.vanilla.mixin.core.server.dedicated;

import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.api.Server;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeBootstrap;
import org.spongepowered.common.SpongeLifecycle;
import org.spongepowered.common.applaunch.config.core.ConfigHandle;
import org.spongepowered.vanilla.VanillaServer;
import org.spongepowered.vanilla.mixin.core.server.MinecraftServerMixin_Vanilla;

@Mixin(DedicatedServer.class)
@Implements(@Interface(iface = Server.class, prefix = "server$"))
public abstract class DedicatedServerMixin_Vanilla extends MinecraftServerMixin_Vanilla implements VanillaServer {

    @Inject(method = "initServer", at = @At("HEAD"))
    private void vanilla$runEngineStartLifecycle(final CallbackInfoReturnable<Boolean> cir) {
        // Save config now that registries have been initialized
        ConfigHandle.setSaveSuppressed(false);

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

        lifecycle.callLoadedGameEvent();
    }

    @Override
    protected void loadLevel() {
        this.shadow$detectBundledResources();
        this.worldManager().loadLevel();
    }
}
