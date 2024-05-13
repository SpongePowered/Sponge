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

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.network.EngineConnectionState;
import org.spongepowered.api.network.ServerSideConnection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.network.ConnectionBridge;
import org.spongepowered.common.bridge.network.ServerLoginPacketListenerImplBridge;
import org.spongepowered.common.network.channel.ConnectionUtil;
import org.spongepowered.common.network.channel.SpongeChannelManager;
import org.spongepowered.common.network.channel.TransactionStore;
import org.spongepowered.common.profile.SpongeGameProfile;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin_Vanilla implements ServerLoginPacketListener {

    // @formatter:off
    @Shadow @Final static Logger LOGGER;

    @Shadow @Final MinecraftServer server;
    @Shadow private ServerLoginPacketListenerImpl.State state;
    @Shadow @Final Connection connection;
    @Shadow @Final private byte[] challenge;
    @Shadow @Nullable String requestedUsername;
    @Shadow private GameProfile authenticatedProfile;

    @Shadow public abstract void shadow$disconnect(Component reason);
    @Shadow abstract void shadow$startClientVerification(GameProfile $$0);
    // @formatter:on

    // Handshake phase:
    // 1. Sync registered plugin channels
    // 2. Post handshake event and plugins can start sending login payloads
    // 3. Wait until the client responded for each of the plugin' requests

    private static final int HANDSHAKE_NOT_STARTED = 0;
    private static final int HANDSHAKE_CLIENT_TYPE = 1;
    private static final int HANDSHAKE_SYNC_CHANNEL_REGISTRATIONS = 2;
    private static final int HANDSHAKE_CHANNEL_REGISTRATION = 3;
    private static final int HANDSHAKE_SYNC_PLUGIN_DATA = 4;

    private int impl$handshakeState = ServerLoginPacketListenerImplMixin_Vanilla.HANDSHAKE_NOT_STARTED;

    @Inject(method = "handleCustomQueryPacket", at = @At("HEAD"), cancellable = true)
    private void impl$onResponsePayload(final ServerboundCustomQueryAnswerPacket packet, final CallbackInfo ci) {
        ci.cancel();

        final SpongeChannelManager channelRegistry = (SpongeChannelManager) Sponge.channelManager();
        this.server.execute(() -> channelRegistry.handleLoginResponsePayload(((ConnectionBridge) this.connection).bridge$getEngineConnection(), (EngineConnectionState) this, packet));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void impl$onTick(final CallbackInfo ci) {
        if (this.state == ServerLoginPacketListenerImpl.State.NEGOTIATING) {
            final ServerSideConnection connection = (ServerSideConnection) ((ConnectionBridge) this.connection).bridge$getEngineConnection();
            if (this.impl$handshakeState == ServerLoginPacketListenerImplMixin_Vanilla.HANDSHAKE_NOT_STARTED) {
                this.impl$handshakeState = ServerLoginPacketListenerImplMixin_Vanilla.HANDSHAKE_CLIENT_TYPE;

                ((SpongeChannelManager) Sponge.channelManager()).requestClientType(connection).thenAccept(result -> {
                    this.impl$handshakeState = ServerLoginPacketListenerImplMixin_Vanilla.HANDSHAKE_SYNC_CHANNEL_REGISTRATIONS;
                });

            } else if (this.impl$handshakeState == ServerLoginPacketListenerImplMixin_Vanilla.HANDSHAKE_SYNC_CHANNEL_REGISTRATIONS) {
                this.impl$handshakeState = ServerLoginPacketListenerImplMixin_Vanilla.HANDSHAKE_CHANNEL_REGISTRATION;

                ((SpongeChannelManager) Sponge.channelManager()).sendLoginChannelRegistry(connection).thenAccept(result -> {
                    final Cause cause = Cause.of(EventContext.empty(), this);
                    final ServerSideConnectionEvent.Handshake event =
                            SpongeEventFactory.createServerSideConnectionEventHandshake(cause, connection, SpongeGameProfile.of(this.authenticatedProfile));
                    SpongeCommon.post(event);
                    this.impl$handshakeState = ServerLoginPacketListenerImplMixin_Vanilla.HANDSHAKE_SYNC_PLUGIN_DATA;
                });
            } else if (this.impl$handshakeState == ServerLoginPacketListenerImplMixin_Vanilla.HANDSHAKE_SYNC_PLUGIN_DATA) {
                final TransactionStore store = ConnectionUtil.getTransactionStore(connection);
                if (store.isEmpty()) {
                    this.state = ServerLoginPacketListenerImpl.State.VERIFYING;
                }
            }
        }
    }

    /**
     * @author aromaa
     * @reason Use thread pool
     */
    @Overwrite
    public void handleKey(final ServerboundKeyPacket packet) {
        Validate.validState(this.state == ServerLoginPacketListenerImpl.State.KEY, "Unexpected key packet");

        final String $$5;
        try {
            final PrivateKey $$1 = this.server.getKeyPair().getPrivate();
            if (!packet.isChallengeValid(this.challenge, $$1)) {
                throw new IllegalStateException("Protocol error");
            }

            final SecretKey $$2 = packet.getSecretKey($$1);
            final Cipher $$3 = Crypt.getCipher(2, $$2);
            final Cipher $$4 = Crypt.getCipher(1, $$2);
            $$5 = new BigInteger(Crypt.digestData("", this.server.getKeyPair().getPublic(), $$2)).toString(16);
            this.state = ServerLoginPacketListenerImpl.State.AUTHENTICATING;
            this.connection.setEncryptionKey($$3, $$4);
        } catch (CryptException var7) {
            throw new IllegalStateException("Protocol error", var7);
        }

        //Sponge start
        ((ServerLoginPacketListenerImplBridge)this).bridge$getExecutor().submit(() -> {
            //Sponge end
            final String username = Objects.requireNonNull(this.requestedUsername, "Player name not initialized");

            try {
                final ProfileResult $$1 = ServerLoginPacketListenerImplMixin_Vanilla.this.server.getSessionService().hasJoinedServer(username, $$5, this.vanilla$getAddress());
                if ($$1 != null) {
                    final GameProfile $$2 = $$1.profile();
                    ServerLoginPacketListenerImplMixin_Vanilla.LOGGER.info("UUID of player {} is {}", $$2.getName(), $$2.getId());
                    ServerLoginPacketListenerImplMixin_Vanilla.this.shadow$startClientVerification($$2);
                } else if (ServerLoginPacketListenerImplMixin_Vanilla.this.server.isSingleplayer()) {
                    ServerLoginPacketListenerImplMixin_Vanilla.LOGGER.warn("Failed to verify username but will let them in anyway!");
                    ServerLoginPacketListenerImplMixin_Vanilla.this.shadow$startClientVerification(UUIDUtil.createOfflineProfile(username));
                } else {
                    ServerLoginPacketListenerImplMixin_Vanilla.this.shadow$disconnect(Component.translatable("multiplayer.disconnect.unverified_username"));
                    ServerLoginPacketListenerImplMixin_Vanilla.LOGGER.error("Username '{}' tried to join with an invalid session", username);
                }
            } catch (AuthenticationUnavailableException var4) {
                if (ServerLoginPacketListenerImplMixin_Vanilla.this.server.isSingleplayer()) {
                    ServerLoginPacketListenerImplMixin_Vanilla.LOGGER.warn("Authentication servers are down but will let them in anyway!");
                    ServerLoginPacketListenerImplMixin_Vanilla.this.shadow$startClientVerification(UUIDUtil.createOfflineProfile(username));
                } else {
                    ServerLoginPacketListenerImplMixin_Vanilla.this.shadow$disconnect(Component.translatable("multiplayer.disconnect.authservers_down"));
                    ServerLoginPacketListenerImplMixin_Vanilla.LOGGER.error("Couldn't verify username because servers are unavailable");
                }
            }
        });
    }

    @Nullable
    private InetAddress vanilla$getAddress() {
        SocketAddress $$0 = this.connection.getRemoteAddress();
        return this.server.getPreventProxyConnections() && $$0 instanceof InetSocketAddress
                ? ((InetSocketAddress)$$0).getAddress()
                : null;
    }
}
