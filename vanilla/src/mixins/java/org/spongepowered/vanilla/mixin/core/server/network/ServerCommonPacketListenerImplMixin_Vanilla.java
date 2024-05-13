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
package org.spongepowered.vanilla.mixin.core.server.network;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.EngineConnectionState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.network.ConnectionBridge;
import org.spongepowered.common.network.channel.SpongeChannelManager;

@Mixin(value = ServerCommonPacketListenerImpl.class, priority = 999)
public abstract class ServerCommonPacketListenerImplMixin_Vanilla {

    // @formatter: off
    @Shadow @Final protected MinecraftServer server;
    @Shadow @Final protected Connection connection;
    // @formatter: on

    @Inject(method = "handleCustomPayload", at = @At(value = "HEAD"))
    private void vanilla$onHandleCustomPayload(final ServerboundCustomPayloadPacket packet, final CallbackInfo ci) {
        final SpongeChannelManager channelRegistry = (SpongeChannelManager) Sponge.channelManager();
        this.server.execute(() -> channelRegistry.handlePlayPayload(((ConnectionBridge) this.connection).bridge$getEngineConnection(), (EngineConnectionState) this, packet.payload()));
    }
}
