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
package org.spongepowered.common.mixin.core.network;

import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.local.LocalAddress;
import net.minecraft.network.chat.Component;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeMinecraftVersion;
import org.spongepowered.common.bridge.network.ConnectionBridge;
import org.spongepowered.common.entity.player.ClientType;
import org.spongepowered.common.network.channel.TransactionStore;
import org.spongepowered.common.util.Constants;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Set;

import javax.annotation.Nullable;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;

@Mixin(Connection.class)
public abstract class ConnectionMixin extends SimpleChannelInboundHandler<Packet<?>> implements ConnectionBridge {

    @Shadow private PacketListener packetListener;
    @Shadow private Channel channel;
    @Shadow public abstract SocketAddress getRemoteAddress();

    private final TransactionStore impl$transactionStore = new TransactionStore(() -> (EngineConnection) this.packetListener);
    private final Set<ResourceKey> impl$registeredChannels = Sets.newConcurrentHashSet();

    @Nullable private InetSocketAddress impl$virtualHost;
    @Nullable private MinecraftVersion impl$version;
    @Nullable private net.minecraft.network.chat.Component impl$kickReason;

    private ClientType impl$clientType = ClientType.VANILLA;

    @Override
    public TransactionStore bridge$getTransactionStore() {
        return this.impl$transactionStore;
    }

    @Override
    public Set<ResourceKey> bridge$getRegisteredChannels() {
        return this.impl$registeredChannels;
    }

    @Override
    public ClientType bridge$getClientType() {
        return this.impl$clientType;
    }

    @Override
    public void bridge$setClientType(final ClientType clientType) {
        this.impl$clientType = clientType;
    }

    @Override
    public InetSocketAddress bridge$getAddress() {
        final SocketAddress remoteAddress = this.getRemoteAddress();
        if (remoteAddress instanceof LocalAddress) { // Single player
            return Constants.Networking.LOCALHOST;
        }
        return (InetSocketAddress) remoteAddress;
    }

    @Override
    public InetSocketAddress bridge$getVirtualHost() {
        if (this.impl$virtualHost != null) {
            return this.impl$virtualHost;
        }
        final SocketAddress local = this.channel.localAddress();
        if (local instanceof LocalAddress) {
            return Constants.Networking.LOCALHOST;
        }
        return (InetSocketAddress) local;
    }

    @Override
    public void bridge$setVirtualHost(final String host, final int port) {
        try {
            this.impl$virtualHost = new InetSocketAddress(InetAddress.getByAddress(host,
                    ((InetSocketAddress) this.channel.localAddress()).getAddress().getAddress()), port);
        } catch (UnknownHostException e) {
            this.impl$virtualHost = InetSocketAddress.createUnresolved(host, port);
        }
    }

    @Nullable
    public Component bridge$getKickReason() {
        return this.impl$kickReason;
    }

    @Override
    public void bridge$setKickReason(Component component) {
        this.impl$kickReason = component;
    }

    @Override
    public MinecraftVersion bridge$getVersion() {
        return this.impl$version;
    }

    @Override
    public void bridge$setVersion(final int version) {
        this.impl$version = new SpongeMinecraftVersion(String.valueOf(version), version);
    }

}
