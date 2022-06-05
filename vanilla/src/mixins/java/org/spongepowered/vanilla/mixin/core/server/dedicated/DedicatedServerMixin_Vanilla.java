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
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.applaunch.config.core.ConfigHandle;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.launch.Lifecycle;
import org.spongepowered.vanilla.mixin.core.server.MinecraftServerMixin_Vanilla;

import java.lang.reflect.InvocationTargetException;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin_Vanilla extends MinecraftServerMixin_Vanilla {

    @Inject(method = "initServer", at = @At("HEAD"))
    private void vanilla$runEngineStartLifecycle(final CallbackInfoReturnable<Boolean> cir) {
        // Save config now that registries have been initialized
        ConfigHandle.setSaveSuppressed(false);

        final Lifecycle lifecycle = Launch.instance().lifecycle();
        lifecycle.establishServerServices();

        lifecycle.establishServerFeatures();

        lifecycle.establishServerRegistries(this);
        lifecycle.callStartingEngineEvent(this);

        this.vanilla$forceSlf4Jreinit();
    }

    @Inject(method = "initServer", at = @At("RETURN"))
    private void vanilla$callStartedEngineAndLoadedGame(final CallbackInfoReturnable<Boolean> cir) {
        final Lifecycle lifecycle = Launch.instance().lifecycle();
        lifecycle.callStartedEngineEvent(this);

        lifecycle.callLoadedGameEvent();
    }

    private void vanilla$forceSlf4Jreinit() {
        // https://github.com/SpongePowered/Sponge/issues/3696
        // Force slf4j to re-find the Log4j service provider, as sometimes it's not been found by this point
        try {
            final var method = LoggerFactory.class.getDeclaredMethod("performInitialization");
            method.setAccessible(true);
            method.invoke(null);
            method.setAccessible(false);
        } catch (final NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LoggerFactory.getLogger(this.getClass()).error("Unable to replace logger", e);
        }
    }
}
