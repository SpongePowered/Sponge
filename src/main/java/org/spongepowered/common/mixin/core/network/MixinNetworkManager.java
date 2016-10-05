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

import io.netty.channel.Channel;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.local.LocalAddress;
import net.minecraft.network.NetworkManager;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeMinecraftVersion;
import org.spongepowered.common.interfaces.IMixinNetworkManager;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

@SuppressWarnings("rawtypes")
@Mixin(NetworkManager.class)
public abstract class MixinNetworkManager extends SimpleChannelInboundHandler implements RemoteConnection, IMixinNetworkManager {

    @Shadow private Channel channel;

    @Shadow public abstract SocketAddress getRemoteAddress();

    private InetSocketAddress virtualHost;
    private MinecraftVersion version;

    private static final InetSocketAddress localhost = InetSocketAddress.createUnresolved("127.0.0.1", 0);

    @Override
    public InetSocketAddress getAddress() {
        SocketAddress remoteAddress = getRemoteAddress();
        if (remoteAddress instanceof LocalAddress) { // Single player
            return localhost;
        }
        return (InetSocketAddress) remoteAddress;
    }

    @Override
    public InetSocketAddress getVirtualHost() {
        if (this.virtualHost != null) {
            return this.virtualHost;
        }
        SocketAddress local = this.channel.localAddress();
        if (local instanceof LocalAddress) {
            return localhost;
        }
        return (InetSocketAddress) local;
    }

    @Override
    public void setVirtualHost(String host, int port) {
        try {
            this.virtualHost = new InetSocketAddress(InetAddress.getByAddress(host,
                    ((InetSocketAddress) this.channel.localAddress()).getAddress().getAddress()), port);
        } catch (UnknownHostException e) {
            this.virtualHost = InetSocketAddress.createUnresolved(host, port);
        }
    }

    @Override
    public MinecraftVersion getVersion() {
        return this.version;
    }

    @Override
    public void setVersion(int version) {
        this.version = new SpongeMinecraftVersion(String.valueOf(version), version);
    }
}
