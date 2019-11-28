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

import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.server.SPacketDisconnect;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.network.NetHandlerLoginServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.network.NetHandlerLoginServerBridge;
import org.spongepowered.common.text.SpongeTexts;

import java.net.SocketAddress;
import java.util.Optional;

@Mixin(NetHandlerLoginServer.class)
public abstract class NetHandlerLoginServerMixin implements NetHandlerLoginServerBridge {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final public NetworkManager networkManager;
    @Shadow private com.mojang.authlib.GameProfile loginGameProfile;

    @Shadow public abstract String getConnectionInfo();
    @Shadow protected abstract com.mojang.authlib.GameProfile getOfflineProfile(com.mojang.authlib.GameProfile profile);

    @Redirect(method = "tryAcceptPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/management/PlayerList;allowUserToConnect(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Ljava/lang/String;"))
    private String impl$ignoreConnections(final PlayerList confMgr, final SocketAddress address, final com.mojang.authlib.GameProfile profile) {
        return null; // We handle disconnecting
    }

    private void impl$closeConnection(final ITextComponent reason) {
        try {
            LOGGER.info("Disconnecting " + this.getConnectionInfo() + ": " + reason.func_150260_c());
            this.networkManager.func_179290_a(new SPacketDisconnect(reason));
            this.networkManager.func_150718_a(reason);
        } catch (Exception exception) {
            LOGGER.error("Error whilst disconnecting player", exception);
        }
    }

    private void impl$disconnectClient(final Optional<Text> disconnectMessage) {
        ITextComponent reason = null;
        if (disconnectMessage.isPresent()) {
            reason = SpongeTexts.toComponent(disconnectMessage.get());
        } else {
            reason = new TextComponentTranslation("disconnect.disconnected");
        }
        this.impl$closeConnection(reason);
    }

    @Override
    public boolean bridge$fireAuthEvent() {
        final Text disconnectMessage = Text.of("You are not allowed to log in to this server.");
        // Cause is created directly as we can't access the cause stack manager
        // from off the main thread
        final ClientConnectionEvent.Auth event = SpongeEventFactory.createClientConnectionEventAuth(
                Cause.of(EventContext.empty(), this.loginGameProfile), (RemoteConnection) this.networkManager,
                new MessageEvent.MessageFormatter(disconnectMessage), (GameProfile) this.loginGameProfile, false
        );
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            this.impl$disconnectClient(event.isMessageCancelled() ? Optional.empty() : Optional.of(event.getMessage()));
        }
        return event.isCancelled();
    }

    @Inject(method = "processLoginStart",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/server/network/NetHandlerLoginServer;currentLoginState:Lnet/minecraft/server/network/NetHandlerLoginServer$LoginState;",
            opcode = Opcodes.PUTFIELD,
            ordinal = 1),
        cancellable = true)
    private void impl$fireAuthEventOffline(final CallbackInfo ci) {
        // Move this check up here, so that the UUID isn't null when we fire the event
        if (!this.loginGameProfile.isComplete()) {
            this.loginGameProfile = this.getOfflineProfile(this.loginGameProfile);
        }

        if (this.bridge$fireAuthEvent()) {
            ci.cancel();
        }
    }
}
