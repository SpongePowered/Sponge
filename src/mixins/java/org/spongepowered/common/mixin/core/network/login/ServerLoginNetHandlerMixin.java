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
package org.spongepowered.common.mixin.core.network.login;

import net.kyori.adventure.text.Component;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.ServerLoginNetHandler;
import net.minecraft.network.play.server.SDisconnectPacket;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.ITextComponent;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
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
import org.spongepowered.common.bridge.network.NetworkManagerHolderBridge;
import org.spongepowered.common.bridge.network.ServerLoginNetHandlerBridge;

import java.net.SocketAddress;

@Mixin(ServerLoginNetHandler.class)
public abstract class ServerLoginNetHandlerMixin implements ServerLoginNetHandlerBridge, NetworkManagerHolderBridge {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final public NetworkManager connection;
    @Shadow private com.mojang.authlib.GameProfile gameProfile;

    @Shadow public abstract String shadow$getUserName();
    @Shadow protected abstract com.mojang.authlib.GameProfile shadow$createFakeProfile(com.mojang.authlib.GameProfile profile);

    @Override
    public NetworkManager bridge$getConnection() {
        return this.connection;
    }

    @Redirect(method = "handleAcceptedLogin()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/management/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/util/text/ITextComponent;"))
    private ITextComponent impl$ignoreConnections(final PlayerList confMgr, final SocketAddress address, final com.mojang.authlib.GameProfile profile) {
        return null; // We handle disconnecting
    }

    private void impl$closeConnection(final ITextComponent reason) {
        try {
            ServerLoginNetHandlerMixin.LOGGER.info("Disconnecting " + this.shadow$getUserName() + ": " + reason.getString());
            this.connection.send(new SDisconnectPacket(reason));
            this.connection.disconnect(reason);
        } catch (Exception exception) {
            ServerLoginNetHandlerMixin.LOGGER.error("Error whilst disconnecting player", exception);
        }
    }

    private void impl$disconnectClient(final Component disconnectMessage) {
        final ITextComponent reason = SpongeAdventure.asVanilla(disconnectMessage);
        this.impl$closeConnection(reason);
    }

    @Override
    public boolean bridge$fireAuthEvent() {
        final Component disconnectMessage = Component.text("You are not allowed to log in to this server.");
        final Cause cause = Cause.of(EventContext.empty(), this);
        final ServerSideConnectionEvent.Auth event = SpongeEventFactory.createServerSideConnectionEventAuth(
                cause, disconnectMessage, disconnectMessage, (ServerSideConnection) this);
        SpongeCommon.postEvent(event);
        if (event.isCancelled()) {
            this.impl$disconnectClient(event.getMessage());
        }
        return event.isCancelled();
    }

    @Inject(method = "handleHello(Lnet/minecraft/network/login/client/CLoginStartPacket;)V",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/network/login/ServerLoginNetHandler;state:Lnet/minecraft/network/login/ServerLoginNetHandler$State;",
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
