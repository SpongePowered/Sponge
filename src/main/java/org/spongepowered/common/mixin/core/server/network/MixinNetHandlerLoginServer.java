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
import net.minecraft.network.login.server.S00PacketDisconnect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.server.network.NetHandlerLoginServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.IMixinNetHandlerLoginServer;
import org.spongepowered.common.text.SpongeTexts;

import java.net.SocketAddress;
import java.util.Optional;

@Mixin(NetHandlerLoginServer.class)
public abstract class MixinNetHandlerLoginServer implements IMixinNetHandlerLoginServer {

    @Shadow private static Logger logger;
    @Shadow public NetworkManager networkManager;
    @Shadow private MinecraftServer server;
    @Shadow private com.mojang.authlib.GameProfile loginGameProfile;

    @Shadow public abstract String getConnectionInfo();
    @Shadow public abstract com.mojang.authlib.GameProfile getOfflineProfile(com.mojang.authlib.GameProfile profile);

    @Redirect(method = "tryAcceptPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/ServerConfigurationManager;"
            + "allowUserToConnect(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Ljava/lang/String;"))
    public String onAllowUserToConnect(ServerConfigurationManager confMgr, SocketAddress address, com.mojang.authlib.GameProfile profile) {
        return null; // We handle disconnecting
    }

    private void closeConnection(IChatComponent reason) {
        try {
            logger.info("Disconnecting " + this.getConnectionInfo() + ": " + reason.getUnformattedText());
            this.networkManager.sendPacket(new S00PacketDisconnect(reason));
            this.networkManager.closeChannel(reason);
        } catch (Exception exception) {
            logger.error("Error whilst disconnecting player", exception);
        }
    }

    private void disconnectClient(Optional<Text> disconnectMessage) {
        IChatComponent reason = null;
        if (disconnectMessage.isPresent()) {
            reason = SpongeTexts.toComponent(disconnectMessage.get());
        } else {
            reason = new ChatComponentTranslation("disconnect.disconnected");
        }
        this.closeConnection(reason);
    }

    @Override
    public boolean fireAuthEvent() {
        Optional<Text> disconnectMessage = Optional.of(Text.of("You are not allowed to log in to this server."));
        ClientConnectionEvent.Auth event = SpongeEventFactory.createClientConnectionEventAuth(Cause.of(NamedCause.source(this.loginGameProfile)),
            disconnectMessage, disconnectMessage, (RemoteConnection) this.networkManager, (GameProfile) this.loginGameProfile);
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            this.disconnectClient(event.getMessage());
        }
        return event.isCancelled();
    }

    @Inject(method = "processLoginStart", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/NetHandlerLoginServer;"
            + "currentLoginState:Lnet/minecraft/server/network/NetHandlerLoginServer$LoginState;",
            opcode = Opcodes.PUTFIELD, ordinal = 1), cancellable = true)
    public void fireAuthEventOffline(CallbackInfo ci) {
        // Move this check up here, so that the UUID isn't null when we fire the event
        if (!this.loginGameProfile.isComplete()) {
            this.loginGameProfile = this.getOfflineProfile(this.loginGameProfile);
        }

        if (this.fireAuthEvent()) {
            ci.cancel();
        }
    }
}
