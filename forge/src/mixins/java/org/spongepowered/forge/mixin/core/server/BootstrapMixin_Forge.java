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
package org.spongepowered.forge.mixin.core.server;

import com.google.inject.Injector;
import com.google.inject.Stage;
import net.minecraft.server.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeLifecycle;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.inject.SpongeGuice;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.forge.hook.ForgeEventHooks;
import org.spongepowered.forge.launch.ForgeLaunch;

@Mixin(Bootstrap.class)
public abstract class BootstrapMixin_Forge {

    // @formatter:off
    @Shadow private static boolean isBootstrapped;
    // @formatter:on

    @Inject(method = "bootStrap", at = @At("HEAD"))
    private static void forge$startLifecycle(final CallbackInfo ci) {
        if (BootstrapMixin_Forge.isBootstrapped) {
            return;
        }
        final ForgeLaunch launch = Launch.instance();
        final Stage stage = SpongeGuice.getInjectorStage(launch.injectionStage());
        SpongeCommon.logger().debug("Creating injector in stage '{}'", stage);
        final Injector bootstrapInjector = launch.createInjector();
        final SpongeLifecycle lifecycle = bootstrapInjector.getInstance(SpongeLifecycle.class);
        launch.setLifecycle(lifecycle);
        lifecycle.establishFactories();
        lifecycle.establishBuilders();
        lifecycle.initTimings();
    }
}
