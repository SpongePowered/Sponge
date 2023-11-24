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
package org.spongepowered.common.mixin.core.server.network;

import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.accessor.server.network.ServerLoginPacketListenerImplAccessor;
import org.spongepowered.common.bridge.server.network.ServerLoginPacketListenerImplBridge;

@Mixin(targets = "net/minecraft/server/network/ServerLoginPacketListenerImpl$1")
public abstract class ServerLoginPacketListenerImpl_1Mixin extends Thread {

    // @formatter:off
    @Shadow @Final private ServerLoginPacketListenerImpl this$0;
    // @formatter:on

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "run()V", at = @At(value = "JUMP", opcode = Opcodes.IFNULL, ordinal = 0, shift = At.Shift.AFTER),
            remap = false, cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void impl$fireAuthEvent(final CallbackInfo ci, final String $$0, final ProfileResult $$1) {
        ((ServerLoginPacketListenerImplAccessor)this$0).accessor$gameProfile($$1.profile());
        if (((ServerLoginPacketListenerImplBridge) this.this$0).bridge$fireAuthEvent()) {
            ci.cancel();
        }
    }
}
