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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.authlib.GameProfile;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.slf4j.Logger;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
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
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.network.ConnectionHolderBridge;
import org.spongepowered.common.bridge.network.ServerLoginPacketListenerImplBridge;

import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin implements ConnectionHolderBridge, ServerLoginPacketListenerImplBridge {

    // @formatter:off
    @Shadow @Final static Logger LOGGER;

    @Shadow @Final Connection connection;
    @Shadow com.mojang.authlib.GameProfile authenticatedProfile;
    @Shadow @Final MinecraftServer server;
    @Shadow private ServerLoginPacketListenerImpl.State state;

    @Shadow public abstract void shadow$disconnect(Component reason);
    // @formatter:on

    private static final ExecutorService impl$EXECUTOR = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
            .setNameFormat("Sponge-LoginThread-%d")
            .setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER))
            .build());

    private boolean impl$accepted = false;

    @Override
    public Connection bridge$getConnection() {
        return this.connection;
    }

    @Redirect(method = "verifyLoginAndFinishConnectionSetup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/network/chat/Component;"))
    private Component impl$onCanPlayerLogin(final PlayerList instance, final SocketAddress $$0, final GameProfile $$1) {
        //We check for this on the configuration handler, skip here for now.
        return null;
    }

    private void impl$disconnectClient(final net.kyori.adventure.text.Component disconnectMessage) {
        final Component reason = SpongeAdventure.asVanilla(disconnectMessage);
        this.shadow$disconnect(reason);
    }

    @Inject(method = "startClientVerification(Lcom/mojang/authlib/GameProfile;)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;state:Lnet/minecraft/server/network/ServerLoginPacketListenerImpl$State;"),
            cancellable = true)
    private void impl$handleAuthEventCancellation(final CallbackInfo ci) {
        ci.cancel();

        if (this.server.usesAuthentication() && !this.connection.isMemoryConnection()) {
            //We are already off the network thread for online servers.
            this.bridge$fireAuthEvent();
        } else {
            //Execute the Auth event off the network thread to not block it.
            this.bridge$getExecutor().submit(this::bridge$fireAuthEvent);
        }
    }

    @Override
    public ExecutorService bridge$getExecutor() {
        return ServerLoginPacketListenerImplMixin.impl$EXECUTOR;
    }

    @Override
    public void bridge$fireAuthEvent() {
        final net.kyori.adventure.text.Component disconnectMessage = net.kyori.adventure.text.Component.text("You are not allowed to log in to this server.");
        final Cause cause = Cause.of(EventContext.empty(), this);
        final ServerSideConnectionEvent.Auth event = SpongeEventFactory.createServerSideConnectionEventAuth(
                cause, disconnectMessage, disconnectMessage, (ServerSideConnection) this);
        SpongeCommon.post(event);
        if (event.isCancelled()) {
            this.impl$disconnectClient(event.message());
            return;
        }

        this.state = ServerLoginPacketListenerImpl.State.NEGOTIATING;
    }
}
