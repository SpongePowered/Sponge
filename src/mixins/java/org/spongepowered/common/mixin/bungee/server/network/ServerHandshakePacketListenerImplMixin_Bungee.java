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
package org.spongepowered.common.mixin.bungee.server.network;

import com.google.gson.Gson;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.accessor.network.ConnectionAccessor;
import org.spongepowered.common.accessor.network.protocol.handshake.ClientIntentionPacketAccessor;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.bridge.network.NetworkManagerBridge_Bungee;

import java.net.InetSocketAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;

@Mixin(ServerHandshakePacketListenerImpl.class)
public abstract class ServerHandshakePacketListenerImplMixin_Bungee {

    private static final Gson gson = new Gson();

    @Shadow @Final private Connection connection;

    @Inject(method = "handleIntention", at = @At(value = "HEAD"), cancellable = true)
    private void bungee$patchHandshake(final ClientIntentionPacket packet, final CallbackInfo ci) {
        if (SpongeConfigs.getCommon().get().bungeecord.ipForwarding && packet.getIntention().equals(ConnectionProtocol.LOGIN)) {
            final String ip = ((ClientIntentionPacketAccessor) packet).accessor$hostName();
            final String[] split = ip.split("\00\\|", 2)[0].split("\00"); // ignore any extra data

            if (split.length == 3 || split.length == 4) {
                ((ClientIntentionPacketAccessor) packet).accessor$hostName(split[0]);
                ((ConnectionAccessor) this.connection).accessor$address(new InetSocketAddress(split[1],
                        ((InetSocketAddress) this.connection.getRemoteAddress()).getPort()));
                ((NetworkManagerBridge_Bungee) this.connection).bungeeBridge$setSpoofedUUID(UUIDTypeAdapter.fromString(split[2]));

                if (split.length == 4) {
                    ((NetworkManagerBridge_Bungee) this.connection).bungeeBridge$setSpoofedProfile(ServerHandshakePacketListenerImplMixin_Bungee.gson
                        .fromJson(split[3], Property[].class));
                }
            } else {
                final TextComponent chatcomponenttext =
                        new TextComponent("If you wish to use IP forwarding, please enable it in your BungeeCord config as well!");
                this.connection.disconnect(chatcomponenttext);
            }
        }
    }
}
