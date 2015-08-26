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

import com.google.common.base.Optional;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.server.S00PacketDisconnect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.server.network.NetHandlerLoginServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.GameProfile;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.source.network.GameClientAuthEvent;
import org.spongepowered.api.event.source.network.GameClientConnectEvent;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.interfaces.IMixinNetHandlerLoginServer;
import org.spongepowered.common.interfaces.IMixinNetworkManager;
import org.spongepowered.common.text.SpongeTexts;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Mixin(NetHandlerLoginServer.class)
public abstract class MixinNetHandlerLoginServer implements RemoteConnection, IMixinNetHandlerLoginServer {

    @Shadow private static Logger logger;
    @Shadow public NetworkManager networkManager;
    @Shadow private MinecraftServer server;
    @Shadow private com.mojang.authlib.GameProfile loginGameProfile;

    private GameClientConnectEvent clientConEvent;

    @Shadow
    abstract public String getConnectionInfo();

    @Redirect(method = "tryAcceptPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/ServerConfigurationManager;"
            + "allowUserToConnect(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Ljava/lang/String;"))
    public String onAllowUserToConnect(ServerConfigurationManager confMgr, SocketAddress address, com.mojang.authlib.GameProfile profile) {
        String kickReason = confMgr.allowUserToConnect(address, profile);
        Text disconnectMessage = null;
        Cause disconnectCause = null;
        if (kickReason != null) {
            disconnectMessage = Texts.of(kickReason);
            // TODO actually make a proper cause
            disconnectCause = Cause.of(kickReason);
        }
        this.clientConEvent =
                SpongeEventFactory.createGameClientConnect(Sponge.getGame(), this, (GameProfile) profile, disconnectMessage, disconnectCause);
        if (kickReason != null) {
            this.clientConEvent.setCancelled(true);
        }
        Sponge.getGame().getEventManager().post(this.clientConEvent);
        return null; // We handle disconnecting
    }

    /**
     * The &#64;At target positions this inject to directly below the above
     * redirect. It can't be handled in the same method because the callback
     * info is not available in a redirect.
     *
     * @param ci Callback info
     */
    @Inject(method = "tryAcceptPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/NetHandlerLoginServer;"
            + "closeConnection(Ljava/lang/String;)V", shift = At.Shift.BY, by = -6), cancellable = true)
    public void onTryAcceptPlayer(CallbackInfo ci) {
        if (this.clientConEvent.isCancelled()) {
            disconnectClient(this.clientConEvent.getDisconnectMessage());
            ci.cancel();
        }
        this.clientConEvent = null;
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
    public InetSocketAddress getAddress() {
        return ((IMixinNetworkManager) this.networkManager).getAddress();
    }

    @Override
    public InetSocketAddress getVirtualHost() {
        return ((IMixinNetworkManager) this.networkManager).getVirtualHost();
    }

    @Override
    public boolean fireAuthEvent() {
        GameClientAuthEvent event = SpongeEventFactory.createGameClientAuth(Sponge.getGame(), this, (GameProfile) this.loginGameProfile, null, null);
        Sponge.getGame().getEventManager().post(event);
        if (event.isCancelled()) {
            this.disconnectClient(event.getDisconnectMessage());
        }
        return event.isCancelled();
    }

    @Inject(method = "processLoginStart", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/NetHandlerLoginServer;"
            + "currentLoginState:Lnet/minecraft/server/network/NetHandlerLoginServer$LoginState;",
            opcode = Opcodes.PUTFIELD, ordinal = 1), cancellable = true)
    public void fireAuthEventOffline(CallbackInfo ci) {
        if (this.fireAuthEvent()) {
            ci.cancel();
        }
    }
}
