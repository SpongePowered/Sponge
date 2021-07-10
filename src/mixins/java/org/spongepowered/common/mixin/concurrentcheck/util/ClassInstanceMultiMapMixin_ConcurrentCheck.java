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
package org.spongepowered.common.mixin.concurrentcheck.util;

import net.minecraft.util.ClassInstanceMultiMap;
import org.spongepowered.api.Platform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.hooks.PlatformHooks;

@Mixin(ClassInstanceMultiMap.class)
public abstract class ClassInstanceMultiMapMixin_ConcurrentCheck {

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void concurrentCheck$checkThreadOnAdd(final Object entity, final CallbackInfoReturnable<Boolean> cir) {
        // This class gets used on the client, but we only care about the server
        if (SpongeCommon.game().platform().executionType() != Platform.Type.CLIENT && !PlatformHooks.INSTANCE
            .getGeneralHooks()
            .onServerThread()) {
            Thread.dumpStack();
            SpongeCommon.logger().error("Detected attempt to add entity '" + entity + "' to ClassInheritanceMultiMap asynchronously.\n"
                    + " This is very bad as it can cause ConcurrentModificationException's during a server tick.\n"
                    + " Skipping...");
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void concurrentCheck$checkServerThreadSide(final Object entity, final CallbackInfoReturnable<Boolean> cir) {
        if (SpongeCommon.game().platform().executionType() != Platform.Type.CLIENT && !PlatformHooks.INSTANCE
            .getGeneralHooks()
            .onServerThread()) {
            Thread.dumpStack();
            SpongeCommon.logger().error("Detected attempt to remove entity '" + entity + "' from ClassInheritanceMultiMap asynchronously.\n"
                    + " This is very bad as it can cause ConcurrentModificationException's during a server tick.\n"
                    + " Skipping...");
            cir.setReturnValue(false);
        }
    }
}
