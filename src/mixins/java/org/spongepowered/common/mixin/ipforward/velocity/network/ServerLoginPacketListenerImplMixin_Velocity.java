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
package org.spongepowered.common.mixin.ipforward.velocity.network;

import static com.google.common.base.Preconditions.checkState;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.ipforward.velocity.VelocityForwardingInfo;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin_Velocity {

    // @formatter:off
    @Shadow @Final private MinecraftServer server;
    @Shadow @Final public Connection connection;
    // @formatter:on

    private boolean ipForward$sentForwardingRequest;

    @Inject(method = "handleHello", at = @At("HEAD"), cancellable = true)
    private void velocity$sendVelocityIndicator(final CallbackInfo info) {
        if (!this.server.usesAuthentication()) {
            checkState(!this.ipForward$sentForwardingRequest, "Sent additional login start message!");
            this.ipForward$sentForwardingRequest = true;

            VelocityForwardingInfo.sendQuery((ServerLoginPacketListenerImpl) (Object) this);
        }
    }

}