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
package org.spongepowered.vanilla.mixin.core.server;

import com.google.inject.Injector;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.launch.Launcher;
import org.spongepowered.vanilla.VanillaLifecycle;
import org.spongepowered.vanilla.launch.ServerLauncher;
import org.spongepowered.vanilla.launch.VanillaServer;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin_Vanilla {

    private VanillaLifecycle vanilla$lifecycle;

    @Redirect(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/DedicatedServer;startServerThread()V"))
    private static void vanilla$prepareGameAndLoadPlugins(final DedicatedServer server) {
        final ServerLauncher launcher = Launcher.getInstance();
        launcher.setupInjection((VanillaServer) server);
        launcher.loadPlugins((VanillaServer) server);
        server.startServerThread();
    }

    @Inject(method = "startServerThread", at = @At("HEAD"))
    private void vanilla$bootstrapLifecycle(CallbackInfo ci) {
        final Injector injector = Launcher.getInstance().getPlatformInjector();
        this.vanilla$lifecycle = injector.getInstance(VanillaLifecycle.class);
        this.vanilla$lifecycle.establishServices();
        this.vanilla$lifecycle.registerPluginListeners();
        this.vanilla$lifecycle.callConstructEventToPlugins();
    }
}
