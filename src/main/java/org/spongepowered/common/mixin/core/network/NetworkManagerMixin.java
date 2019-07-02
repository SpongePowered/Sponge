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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeMinecraftVersion;
import org.spongepowered.common.bridge.network.NetworkManagerBridge;
import org.spongepowered.common.util.Constants;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
@Mixin(NetworkManager.class)
public abstract class NetworkManagerMixin extends SimpleChannelInboundHandler implements NetworkManagerBridge {

    @Shadow private Channel channel;

    @Shadow public abstract SocketAddress getRemoteAddress();

    @Nullable private InetSocketAddress impl$virtualHost;
    @Nullable private MinecraftVersion impl$version;

    @Override
    public InetSocketAddress bridge$getAddress() {
        final SocketAddress remoteAddress = getRemoteAddress();
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

    @Override
    public MinecraftVersion bridge$getVersion() {
        return this.impl$version;
    }

    @Override
    public void bridge$setVersion(final int version) {
        this.impl$version = new SpongeMinecraftVersion(String.valueOf(version), version);
    }

}
