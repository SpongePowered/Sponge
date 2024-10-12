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

import net.kyori.adventure.text.Component;
import net.minecraft.network.Connection;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.server.network.MemoryServerHandshakePacketListenerImpl;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.network.ServerSideConnection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.network.ConnectionBridge;
import org.spongepowered.common.bridge.server.network.ServerHandshakePacketListenerImplBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.network.SpongeEngineConnection;

@Mixin(MemoryServerHandshakePacketListenerImpl.class)
public abstract class MemoryServerHandshakePacketListenerImplMixin implements ServerHandshakePacketListenerImplBridge {

    @Shadow @Final private Connection connection;

    @Override
    public boolean bridge$transferred() {
        return false;
    }

    @Inject(method = "handleIntention", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/Connection;setupInboundProtocol(Lnet/minecraft/network/ProtocolInfo;Lnet/minecraft/network/PacketListener;)V"),
            cancellable = true)
    private void impl$onLogin(final CallbackInfo ci) {
        this.connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);
        final SpongeEngineConnection connection = ((ConnectionBridge) this.connection).bridge$getEngineConnection();
        final Component message = Component.text("You are not allowed to log in to this server.");
        final ServerSideConnectionEvent.Intent event = SpongeEventFactory.createServerSideConnectionEventIntent(
                PhaseTracker.getCauseStackManager().currentCause(), message, message, (ServerSideConnection) connection, false);
        if (connection.postGuardedEvent(event)) {
            ci.cancel();
            final net.minecraft.network.chat.Component kickReason = SpongeAdventure.asVanilla(event.message());
            this.connection.send(new ClientboundLoginDisconnectPacket(kickReason));
            this.connection.disconnect(kickReason);
        }
    }

    @Redirect(method = "handleIntention", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;setupOutboundProtocol(Lnet/minecraft/network/ProtocolInfo;)V"))
    private void impl$onSetupOutboundProtocol(final Connection instance, final ProtocolInfo<?> $$0) {
        //Moved to impl$onLogin
    }
}
