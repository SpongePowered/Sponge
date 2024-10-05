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
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.network.ServerSideConnection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.network.ConnectionBridge;
import org.spongepowered.common.bridge.server.network.ServerHandshakePacketListenerImplBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.network.SpongeEngineConnection;
import org.spongepowered.common.util.NetworkUtil;

@Mixin(ServerHandshakePacketListenerImpl.class)
public abstract class ServerHandshakePacketListenerImplMixin implements ServerHandshakePacketListenerImplBridge {

    @Shadow @Final private Connection connection;

    private boolean impl$transferred;

    @Inject(method = "handleIntention", at = @At("HEAD"))
    private void impl$updateVersionAndHost(final ClientIntentionPacket packetIn, final CallbackInfo ci) {
        final ConnectionBridge info = (ConnectionBridge) this.connection;
        info.bridge$setVersion(packetIn.protocolVersion());
        info.bridge$setVirtualHost(NetworkUtil.cleanVirtualHost(packetIn.hostName()), packetIn.port());
    }

    @Override
    public boolean bridge$transferred() {
        return this.impl$transferred;
    }

    @Inject(method = "handleIntention", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerHandshakePacketListenerImpl;beginLogin(Lnet/minecraft/network/protocol/handshake/ClientIntentionPacket;Z)V"),
            slice = @Slice(
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/server/MinecraftServer;getStatus()Lnet/minecraft/network/protocol/status/ServerStatus;")),
            cancellable = true)
    private void impl$onLogin(final ClientIntentionPacket $$0, final CallbackInfo ci) {
        this.connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);
        final SpongeEngineConnection connection = ((ConnectionBridge) this.connection).bridge$getEngineConnection();
        final Component message = Component.text("You are not allowed to log in to this server.");
        final ServerSideConnectionEvent.Intent event = SpongeEventFactory.createServerSideConnectionEventIntent(
                PhaseTracker.getCauseStackManager().currentCause(), message, message, (ServerSideConnection) connection, false);
        if (connection.postGuardedEvent(event)) {
            final net.minecraft.network.chat.Component kickReason = SpongeAdventure.asVanilla(event.message());
            this.connection.send(new ClientboundLoginDisconnectPacket(kickReason));
            this.connection.disconnect(kickReason);
            ci.cancel();
        }
    }

    @Redirect(method = "handleIntention", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;acceptsTransfers()Z"))
    private boolean impl$onTransfer(final MinecraftServer instance) {
        this.connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);
        this.impl$transferred = true;
        final SpongeEngineConnection connection = ((ConnectionBridge) this.connection).bridge$getEngineConnection();
        final Component message = Component.translatable("multiplayer.disconnect.transfers_disabled");
        final ServerSideConnectionEvent.Intent event = SpongeEventFactory.createServerSideConnectionEventIntent(
                PhaseTracker.getCauseStackManager().currentCause(), message, message, (ServerSideConnection) connection, true);
        event.setCancelled(!instance.acceptsTransfers());
        if (connection.postGuardedEvent(event)) {
            ((ConnectionBridge) this.connection).bridge$setKickReason(SpongeAdventure.asVanilla(event.message()));
        }
        return !event.isCancelled();
    }

    @ModifyArg(method = "handleIntention", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/login/ClientboundLoginDisconnectPacket;<init>(Lnet/minecraft/network/chat/Component;)V"))
    private net.minecraft.network.chat.Component impl$setTransferDisconnectMessage(final net.minecraft.network.chat.Component component) {
        return ((ConnectionBridge) this.connection).bridge$getKickReason();
    }

    @Redirect(method = "handleIntention", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/network/Connection;setupOutboundProtocol(Lnet/minecraft/network/ProtocolInfo;)V"),
        slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;acceptsTransfers()Z")))
    private void impl$onSetupOutboundProtocol(final Connection instance, final ProtocolInfo<?> $$0) {
        //Moved to impl$onTransfer
    }

    @Redirect(method = "beginLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;setupOutboundProtocol(Lnet/minecraft/network/ProtocolInfo;)V"))
    private void impl$onSetupOutboundProtocol2(final Connection instance, final ProtocolInfo<?> $$0) {
        //Moved to impl$onLogin
    }
}
