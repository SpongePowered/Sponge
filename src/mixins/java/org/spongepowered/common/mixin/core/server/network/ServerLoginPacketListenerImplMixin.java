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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.players.PlayerList;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.network.ConnectionBridge;
import org.spongepowered.common.bridge.network.ConnectionHolderBridge;
import org.spongepowered.common.bridge.server.network.ServerLoginPacketListenerImplBridge;
import org.spongepowered.common.bridge.server.players.PlayerListBridge;
import org.spongepowered.common.network.channel.SpongeChannelManager;

import java.io.IOException;
import java.util.concurrent.CompletionException;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin implements ServerLoginPacketListenerImplBridge, ConnectionHolderBridge {

    @Shadow @Final public Connection connection;
    @Shadow private com.mojang.authlib.GameProfile gameProfile;
    @Shadow @Final private MinecraftServer server;
    @Shadow private ServerLoginPacketListenerImpl.State state;
    @Shadow private ServerPlayer delayedAcceptPlayer;

    @Shadow protected abstract com.mojang.authlib.GameProfile shadow$createFakeProfile(com.mojang.authlib.GameProfile profile);
    @Shadow public abstract void shadow$disconnect(net.minecraft.network.chat.Component reason);

    @Shadow public abstract void disconnect(net.minecraft.network.chat.Component param0);

    private boolean impl$accepted = false;

    @Override
    public Connection bridge$getConnection() {
        return this.connection;
    }

    /**
     * @author morph - April 27th, 2021
     * @author dualspiral - July 17th, 2021
     *
     * @reason support async ban/whitelist service and user->player syncing.
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
        final PlayerList playerList = this.server.getPlayerList();
        // Sponge end

        // Sponge start - completable future
        ((PlayerListBridge) playerList).bridge$canPlayerLogin(this.connection.getRemoteAddress(), this.gameProfile)
                .handle((componentOpt, throwable) -> {
                    if (throwable != null) {
                        // An error occurred during login checks so we ask to abort.
                        ((ConnectionBridge) this.connection).bridge$setKickReason(new TextComponent("An error occurred checking ban/whitelist status."));
                        SpongeCommon.logger().error("An error occurred when checking the ban/whitelist status of {}.", this.gameProfile.getId().toString());
                        SpongeCommon.logger().error(throwable);
                    } else if (componentOpt != null) {
                        // We handle this later
                        ((ConnectionBridge) this.connection).bridge$setKickReason(componentOpt);
                    }

                    try {
                        ((SpongeServer) SpongeCommon.server()).userManager().handlePlayerLogin(this.gameProfile);
                    } catch (final IOException e) {
                        throw new CompletionException(e);
                    }
                    return null;
                })
                .handleAsync((ignored, throwable) -> {
                    if (throwable != null) {
                        // We're just going to disconnect here, because something went horribly wrong.
                        if (throwable instanceof CompletionException) {
                            throw (CompletionException) throwable;
                        } else {
                            throw new CompletionException(throwable);
                        }
                    }
                    // Sponge end
                    this.state = ServerLoginPacketListenerImpl.State.ACCEPTED;
                    if (this.server.getCompressionThreshold() >= 0 && !this.connection.isMemoryConnection()) {
                        this.connection.send(new ClientboundLoginCompressionPacket(this.server.getCompressionThreshold()), (param0) -> this.connection.setupCompression(this.server.getCompressionThreshold()));
                    }

                    this.connection.send(new ClientboundGameProfilePacket(this.gameProfile));
                    final ServerPlayer var1 = this.server.getPlayerList().getPlayer(this.gameProfile.getId());
                    if (var1 != null) {
                        this.state = ServerLoginPacketListenerImpl.State.DELAY_ACCEPT;
                        this.delayedAcceptPlayer = this.server.getPlayerList().getPlayerForLogin(this.gameProfile);
                    } else {
                        // Sponge start - Also send the channel registrations using the minecraft channel, for compatibility
                        final ServerSideConnection connection = (ServerSideConnection) this;
                        ((SpongeChannelManager) Sponge.channelManager()).sendChannelRegistrations(connection);
                        // Sponge end

                        try {
                            this.server.getPlayerList()
                                    .placeNewPlayer(this.connection, this.server.getPlayerList().getPlayerForLogin(this.gameProfile));
                            // invalidate just to be sure there is no user cached for the online player anymore
                            Sponge.server().userManager().removeFromCache(this.gameProfile.getId());
                        } catch (final Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return null;
                }, SpongeCommon.server()).exceptionally(throwable -> {
                    // Sponge Start
                    // If a throwable exists, we're just going to disconnect the user, better than leaving them in limbo.
                    if (throwable != null) {
                        this.impl$disconnectError(throwable,
                                this.state == ServerLoginPacketListenerImpl.State.ACCEPTED || this.state == ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT);
                    }
                    return null;
                    // Sponge End
                });
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;"
            + "placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void impl$catchErrorWhenTickingNewPlayer(final PlayerList playerList, final Connection param0, final ServerPlayer param1) {
        try {
            playerList.placeNewPlayer(param0, param1);
        } catch (final Exception e) {
            this.impl$disconnectError(e, true);
        }
    }

    private void impl$disconnectError(final Throwable throwable, final boolean gameDisconnect) {
        SpongeCommon.logger().error("Forcibly disconnecting user {} due to an error during login.", this.gameProfile, throwable);
        final net.minecraft.network.chat.Component message = new TextComponent("Internal Server Error: unable to complete login.");
        // At this point, the client might be in the GAME state, so we need to send the right packet.
        if (gameDisconnect) {
            this.connection.send(new ClientboundDisconnectPacket(message), (param1) -> this.connection.disconnect(message));
        } else {
            this.shadow$disconnect(message);
        }
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
