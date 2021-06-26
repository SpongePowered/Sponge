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
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.network.ServerSideConnection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.network.ConnectionBridge;
import org.spongepowered.common.bridge.network.ConnectionHolderBridge;
import org.spongepowered.common.bridge.server.network.ServerLoginPacketListenerImplBridge;
import org.spongepowered.common.bridge.server.players.PlayerListBridge;
import org.spongepowered.common.network.channel.SpongeChannelManager;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin implements ServerLoginPacketListenerImplBridge, ConnectionHolderBridge {

    @Shadow @Final public Connection connection;
    @Shadow private com.mojang.authlib.GameProfile gameProfile;
    @Shadow @Final private MinecraftServer server;
    @Shadow private ServerLoginPacketListenerImpl.State state;
    @Shadow private ServerPlayer delayedAcceptPlayer;

    @Shadow protected abstract com.mojang.authlib.GameProfile shadow$createFakeProfile(com.mojang.authlib.GameProfile profile);
    @Shadow public abstract void shadow$disconnect(net.minecraft.network.chat.Component reason);

    private boolean impl$accepted = false;

    @Override
    public Connection bridge$getConnection() {
        return this.connection;
    }

    /**
     * @author morph - April 27th, 2021
     *
     * @reason support async ban/whitelist service
     */
    @Overwrite
    public void handleAcceptedLogin() {
        if (!this.gameProfile.isComplete()) {
            this.gameProfile = this.shadow$createFakeProfile(this.gameProfile);
        }

        // Sponge start - avoid #tick calling handleAcceptedLogin more than once.
        if (this.impl$accepted) {
            return;
        }
        this.impl$accepted = true;
        // Sponge end

        // Sponge start - completable future
        ((PlayerListBridge) this.server.getPlayerList()).bridge$canPlayerLogin(this.connection.getRemoteAddress(), this.gameProfile).thenAcceptAsync(componentOpt -> {
        // Sponge end
            // Sponge start - we handle this later
            if (componentOpt.isPresent()) {
                // TODO add should fire logic to disconnect the player and return early
                ((ConnectionBridge) this.connection).bridge$setKickReason(componentOpt.get());
            }
            // Sponge end
            this.state = ServerLoginPacketListenerImpl.State.ACCEPTED;
            if (this.server.getCompressionThreshold() >= 0 && !this.connection.isMemoryConnection()) {
                this.connection.send(new ClientboundLoginCompressionPacket(this.server.getCompressionThreshold()), (param0) -> this.connection.setupCompression(this.server.getCompressionThreshold()));
            }

            this.connection.send(new ClientboundGameProfilePacket(this.gameProfile));
            ServerPlayer var1 = this.server.getPlayerList().getPlayer(this.gameProfile.getId());
            if (var1 != null) {
                this.state = ServerLoginPacketListenerImpl.State.DELAY_ACCEPT;
                this.delayedAcceptPlayer = this.server.getPlayerList().getPlayerForLogin(this.gameProfile);
            } else {
                // Sponge start - Also send the channel registrations using the minecraft channel, for compatibility
                final ServerSideConnection connection = (ServerSideConnection) this;
                ((SpongeChannelManager) Sponge.channelManager()).sendChannelRegistrations(connection);
                // Sponge end
                this.server.getPlayerList().placeNewPlayer(this.connection, this.server.getPlayerList().getPlayerForLogin(this.gameProfile));
            }
        }, SpongeCommon.server());
    }

    private void impl$disconnectClient(final Component disconnectMessage) {
        final net.minecraft.network.chat.Component reason = SpongeAdventure.asVanilla(disconnectMessage);
        this.shadow$disconnect(reason);
    }

    @Override
    public boolean bridge$fireAuthEvent() {
        final Component disconnectMessage = Component.text("You are not allowed to log in to this server.");
        final Cause cause = Cause.of(EventContext.empty(), this);
        final ServerSideConnectionEvent.Auth event = SpongeEventFactory.createServerSideConnectionEventAuth(
                cause, disconnectMessage, disconnectMessage, (ServerSideConnection) this);
        SpongeCommon.post(event);
        if (event.isCancelled()) {
            this.impl$disconnectClient(event.message());
        }
        return event.isCancelled();
    }

    @Inject(method = "handleHello(Lnet/minecraft/network/protocol/login/ServerboundHelloPacket;)V",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;state:Lnet/minecraft/server/network/ServerLoginPacketListenerImpl$State;",
            opcode = Opcodes.PUTFIELD,
            ordinal = 1),
        cancellable = true)
    private void impl$fireAuthEventOffline(final CallbackInfo ci) {
        // Move this check up here, so that the UUID isn't null when we fire the event
        if (!this.gameProfile.isComplete()) {
            this.gameProfile = this.shadow$createFakeProfile(this.gameProfile);
        }

        if (this.bridge$fireAuthEvent()) {
            ci.cancel();
        }
    }
}
