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
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.accessor.network.ConnectionAccessor;
import org.spongepowered.common.accessor.network.protocol.login.ClientboundCustomQueryPacketAccessor;
import org.spongepowered.common.accessor.network.protocol.login.ServerboundCustomQueryPacketAccessor;
import org.spongepowered.common.ipforward.velocity.VelocityForwardingInfo;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadLocalRandom;

import static com.google.common.base.Preconditions.checkState;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin_Velocity {

    // @formatter:off
    @Shadow @Final private MinecraftServer server;
    @Shadow @Final public Connection connection;
    @Shadow private GameProfile gameProfile;
    @Shadow private ServerLoginPacketListenerImpl.State state;

    @Shadow public abstract void shadow$disconnect(Component reason);
    // @formatter:on

    private int velocityInfoId = Integer.MAX_VALUE;

    @Inject(method = "handleHello", at = @At("HEAD"), cancellable = true)
    private void velocity$sendVelocityIndicator(final CallbackInfo info) {
        if (!this.server.usesAuthentication()) {
            checkState(this.velocityInfoId == Integer.MAX_VALUE, "Sent additional login start message!");
            this.velocityInfoId = ThreadLocalRandom.current().nextInt();

            final ClientboundCustomQueryPacket playerInfoRequest = new ClientboundCustomQueryPacket();
            final ClientboundCustomQueryPacketAccessor accessor = (ClientboundCustomQueryPacketAccessor) playerInfoRequest;
            accessor.accessor$identifier(VelocityForwardingInfo.PLAYER_INFO_CHANNEL);
            accessor.accessor$transactionId(this.velocityInfoId);
            accessor.accessor$data(new FriendlyByteBuf(Unpooled.EMPTY_BUFFER));

            this.connection.send(playerInfoRequest);
            info.cancel();
        }
    }

    @Inject(method = "handleCustomQueryPacket", at = @At("HEAD"), cancellable = true)
    private void velocity$processPlayerInfoMessage(final ServerboundCustomQueryPacket packet, final CallbackInfo info) {
        final ServerboundCustomQueryPacketAccessor packetAccessor = (ServerboundCustomQueryPacketAccessor) packet;
        if (packetAccessor.accessor$transactionId() == this.velocityInfoId) {
            final FriendlyByteBuf buf = packetAccessor.accessor$data();
            if (buf == null) {
                this.shadow$disconnect(new TextComponent("This server requires you to connect with Velocity."));
                info.cancel();
                return;
            }

            if (!VelocityForwardingInfo.checkIntegrity(buf)) {
                this.shadow$disconnect(new TextComponent("Unable to verify player details"));
                info.cancel();
                return;
            }

            final ConnectionAccessor networkManagerAccessor = (ConnectionAccessor) this.connection;
            networkManagerAccessor.accessor$address(new InetSocketAddress(VelocityForwardingInfo.readAddress(buf), ((InetSocketAddress) this.connection
                .getRemoteAddress()).getPort()));

            this.gameProfile = VelocityForwardingInfo.createProfile(buf);
            this.state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
            info.cancel();
        }
    }

}