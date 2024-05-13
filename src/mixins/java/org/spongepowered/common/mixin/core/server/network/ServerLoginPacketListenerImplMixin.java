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
import org.checkerframework.checker.nullness.qual.Nullable;
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
import org.spongepowered.common.bridge.network.ConnectionBridge;
import org.spongepowered.common.bridge.network.ServerLoginPacketListenerImplBridge;
import org.spongepowered.common.bridge.server.players.PlayerListBridge;
import org.spongepowered.common.network.SpongeEngineConnection;
import org.spongepowered.common.profile.SpongeGameProfile;

import java.net.SocketAddress;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin implements ServerLoginPacketListenerImplBridge {

    // @formatter:off
    @Shadow @Final static Logger LOGGER;

    @Shadow @Final Connection connection;
    @Shadow private com.mojang.authlib.GameProfile authenticatedProfile;
    @Shadow @Final MinecraftServer server;
    @Shadow private ServerLoginPacketListenerImpl.State state;

    @Shadow public abstract void shadow$disconnect(Component reason);
    // @formatter:on

    private static final ExecutorService impl$EXECUTOR = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
            .setNameFormat("Sponge-LoginThread-%d")
            .setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER))
            .build());

    @Override
    public Connection bridge$getConnection() {
        return this.connection;
    }

    @Redirect(method = "verifyLoginAndFinishConnectionSetup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/network/chat/Component;"))
    private Component impl$onCanPlayerLogin(final PlayerList instance, final SocketAddress $$0, final GameProfile $$1) {
        //We check this as part of auth event.
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

        ((PlayerListBridge) this.server.getPlayerList()).bridge$canPlayerLogin(this.connection.getRemoteAddress(), this.authenticatedProfile)
                .handle((componentOpt, throwable) -> {
                    if (throwable != null) {
                        // An error occurred during login checks so we ask to abort.
                        ((ConnectionBridge) this.connection).bridge$setKickReason(Component.literal("An error occurred checking ban/whitelist status."));
                        SpongeCommon.logger().error("An error occurred when checking the ban/whitelist status of {}.", this.authenticatedProfile.getId().toString());
                        SpongeCommon.logger().error(throwable);
                    } else if (componentOpt != null) {
                        // We handle this later
                        ((ConnectionBridge) this.connection).bridge$setKickReason(componentOpt);
                    }
                    return null;
                }).handleAsync((ignored, throwable) -> {
                    if (throwable != null) {
                        // We're just going to disconnect here, because something went horribly wrong.
                        if (throwable instanceof CompletionException) {
                            throw (CompletionException) throwable;
                        } else {
                            throw new CompletionException(throwable);
                        }
                    }
                    this.impl$fireAuthEvent();
                    return null;
                }, this.bridge$getExecutor()).exceptionally(throwable -> {
                    SpongeCommon.logger().error("Forcibly disconnecting user {}({}) due to an error during login.", this.authenticatedProfile.getName(), this.authenticatedProfile.getId(), throwable);
                    this.shadow$disconnect(Component.literal("Internal Server Error: unable to complete login."));
                    return null;
                });
    }

    @Override
    public ExecutorService bridge$getExecutor() {
        return ServerLoginPacketListenerImplMixin.impl$EXECUTOR;
    }

    private void impl$fireAuthEvent() {
        final @Nullable Component kickReason = ((ConnectionBridge) this.connection).bridge$getKickReason();
        final net.kyori.adventure.text.Component disconnectMessage;
        if (kickReason != null) {
            disconnectMessage = SpongeAdventure.asAdventure(kickReason);
        } else {
            disconnectMessage = net.kyori.adventure.text.Component.text("You are not allowed to log in to this server.");
        }
        final SpongeEngineConnection connection = ((ConnectionBridge) this.connection).bridge$getEngineConnection();
        connection.setGameProfile(this.authenticatedProfile);
        final Cause cause = Cause.of(EventContext.empty(), this);
        final ServerSideConnectionEvent.Auth event = SpongeEventFactory.createServerSideConnectionEventAuth(
                cause, disconnectMessage, disconnectMessage, (ServerSideConnection) connection,
                SpongeGameProfile.of(this.authenticatedProfile));
        if (connection.postGuardedEvent(event)) {
            this.impl$disconnectClient(event.message());
            return;
        }

        this.state = ServerLoginPacketListenerImpl.State.NEGOTIATING;
    }
}
