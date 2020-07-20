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
package org.spongepowered.common.mixin.ipforward.velocity.network;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.ServerLoginNetHandler;
import net.minecraft.network.login.client.CCustomPayloadLoginPacket;
import net.minecraft.network.login.server.SCustomPayloadLoginPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.accessor.network.NetworkManagerAccessor;
import org.spongepowered.common.accessor.network.login.client.CCustomPayloadLoginPacketAccessor;
import org.spongepowered.common.accessor.network.login.server.SCustomPayloadLoginPacketAccessor;
import org.spongepowered.common.mixin.ipforward.velocity.VelocityForwardingInfo;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadLocalRandom;

import static com.google.common.base.Preconditions.checkState;

@Mixin(ServerLoginNetHandler.class)
public abstract class ServerLoginNetHandlerMixin_Velocity {

    @Shadow @Final private MinecraftServer server;
    @Shadow @Final public NetworkManager networkManager;
    @Shadow private GameProfile loginGameProfile;
    @Shadow private ServerLoginNetHandler.State currentLoginState;
    private int velocityInfoId = Integer.MAX_VALUE;

    @Inject(method = "processLoginStart", at = @At(value = "HEAD"), cancellable = true)
    private void velocity$sendVelocityIndicator(final CallbackInfo info) {
        if (!this.server.isServerInOnlineMode()) {
            checkState(this.velocityInfoId == Integer.MAX_VALUE, "Sent additional login start message!");
            this.velocityInfoId = ThreadLocalRandom.current().nextInt();

            SCustomPayloadLoginPacket playerInfoRequest = new SCustomPayloadLoginPacket();
            SCustomPayloadLoginPacketAccessor accessor = (SCustomPayloadLoginPacketAccessor) playerInfoRequest;
            accessor.accessor$setChannel(VelocityForwardingInfo.PLAYER_INFO_CHANNEL);
            accessor.accessor$setTransaction(this.velocityInfoId );
            accessor.accessor$setPayload(new PacketBuffer(Unpooled.EMPTY_BUFFER));

            networkManager.sendPacket(playerInfoRequest);
            info.cancel();
        }
    }

    @Inject(method = "processCustomPayloadLogin", at = @At(value = "HEAD"), cancellable = true)
    private void velocity$processPlayerInfoMessage(final CCustomPayloadLoginPacket packet, final CallbackInfo info) {
        CCustomPayloadLoginPacketAccessor packetAccessor = (CCustomPayloadLoginPacketAccessor) packet;
        if (packetAccessor.accessor$getTransaction() == this.velocityInfoId) {
            PacketBuffer buf = packetAccessor.accessor$getPayload();
            if (buf == null) {
                this.disconnect(new StringTextComponent("This server requires you to connect with Velocity."));
                info.cancel();
                return;
            }

            if (!VelocityForwardingInfo.checkIntegrity(buf)) {
                this.disconnect(new StringTextComponent("Unable to verify player details"));
                info.cancel();
                return;
            }

            NetworkManagerAccessor networkManagerAccessor = (NetworkManagerAccessor) this.networkManager;
            networkManagerAccessor.accessor$setSocketAddress(new InetSocketAddress(VelocityForwardingInfo.readAddress(buf), ((InetSocketAddress) this.networkManager.getRemoteAddress()).getPort()));

            this.loginGameProfile = VelocityForwardingInfo.createProfile(buf);
            this.currentLoginState = ServerLoginNetHandler.State.READY_TO_ACCEPT;
            info.cancel();
        }
    }

    @Shadow public abstract void disconnect(ITextComponent reason);
}