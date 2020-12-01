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
package org.spongepowered.vanilla.mixin.core.network.login;

import net.minecraft.network.login.IServerLoginNetHandler;
import net.minecraft.network.login.ServerLoginNetHandler;
import net.minecraft.network.login.client.CCustomPayloadLoginPacket;
import net.minecraft.network.login.client.CLoginStartPacket;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.api.network.ServerSideConnection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.network.channel.ConnectionUtil;
import org.spongepowered.common.network.channel.SpongeChannelRegistry;
import org.spongepowered.common.network.channel.TransactionStore;

@Mixin(ServerLoginNetHandler.class)
public abstract class ServerLoginNetHandlerMixin_Vanilla implements IServerLoginNetHandler {

    @Shadow @Final private MinecraftServer server;
    @Shadow private ServerLoginNetHandler.State currentLoginState;

    // Handshake phase:
    // 1. Sync registered plugin channels
    // 2. Post handshake event and plugins can start sending login payloads
    // 3. Wait until the client responded for each of the plugin' requests

    private static final int HANDSHAKE_NOT_STARTED = 0;
    private static final int HANDSHAKE_CLIENT_TYPE = 1;
    private static final int HANDSHAKE_SYNC_CHANNEL_REGISTRATIONS = 2;
    private static final int HANDSHAKE_SYNC_PLUGIN_DATA = 3;

    private int impl$handshakeState = ServerLoginNetHandlerMixin_Vanilla.HANDSHAKE_NOT_STARTED;

    @Inject(method = "processCustomPayloadLogin", at = @At(value = "HEAD"), cancellable = true)
    private void onResponsePayload(final CCustomPayloadLoginPacket packet, final CallbackInfo ci) {
        ci.cancel();

        final SpongeChannelRegistry channelRegistry = (SpongeChannelRegistry) Sponge.getChannelRegistry();
        this.server.execute(() -> channelRegistry.handleLoginResponsePayload((EngineConnection) this, packet));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void impl$onTick(final CallbackInfo ci) {
        if (this.currentLoginState == ServerLoginNetHandler.State.NEGOTIATING) {
            final ServerSideConnection connection = (ServerSideConnection) this;
            if (this.impl$handshakeState == ServerLoginNetHandlerMixin_Vanilla.HANDSHAKE_NOT_STARTED) {
                this.impl$handshakeState = ServerLoginNetHandlerMixin_Vanilla.HANDSHAKE_CLIENT_TYPE;

                ((SpongeChannelRegistry) Sponge.getChannelRegistry()).requestClientType(connection).thenAccept(result -> {
                    this.impl$handshakeState = ServerLoginNetHandlerMixin_Vanilla.HANDSHAKE_SYNC_CHANNEL_REGISTRATIONS;
                });

            } else if (this.impl$handshakeState == ServerLoginNetHandlerMixin_Vanilla.HANDSHAKE_SYNC_CHANNEL_REGISTRATIONS) {
                ((SpongeChannelRegistry) Sponge.getChannelRegistry()).sendLoginChannelRegistry(connection).thenAccept(result -> {
                    final Cause cause = Cause.of(EventContext.empty(), this);
                    final ServerSideConnectionEvent.Handshake event =
                            SpongeEventFactory.createServerSideConnectionEventHandshake(cause, connection);
                    SpongeCommon.postEvent(event);
                    this.impl$handshakeState = ServerLoginNetHandlerMixin_Vanilla.HANDSHAKE_SYNC_PLUGIN_DATA;
                });
            } else if (this.impl$handshakeState == ServerLoginNetHandlerMixin_Vanilla.HANDSHAKE_SYNC_PLUGIN_DATA) {
                final TransactionStore store = ConnectionUtil.getTransactionStore(connection);
                if (store.isEmpty()) {
                    this.currentLoginState = ServerLoginNetHandler.State.READY_TO_ACCEPT;
                }
            }
        }
    }

    @Inject(method = "tryAcceptPlayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/management/PlayerList;initializeConnectionToPlayer(Lnet/minecraft/network/NetworkManager;Lnet/minecraft/entity/player/ServerPlayerEntity;)V"))
    private void impl$onTryAcceptPlayer_beforeInitPlayer(final CallbackInfo ci) {
        final ServerSideConnection connection = (ServerSideConnection) this;
        // Also send the channel registrations using the minecraft channel, for compatibility
        ((SpongeChannelRegistry) Sponge.getChannelRegistry()).sendChannelRegistrations(connection);
    }

    @Inject(method = "processLoginStart", at = @At(value = "RETURN"))
    private void impl$onProcessLoginStart(final CLoginStartPacket packet, final CallbackInfo ci) {
        if (this.currentLoginState == ServerLoginNetHandler.State.READY_TO_ACCEPT) {
            this.currentLoginState = ServerLoginNetHandler.State.NEGOTIATING;
        }
    }
}
