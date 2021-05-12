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
package org.spongepowered.common.bridge.network;

import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.common.entity.player.ClientType;
import org.spongepowered.common.network.channel.TransactionStore;

import java.net.InetSocketAddress;
import java.util.Set;

public interface ConnectionBridge {

    TransactionStore bridge$getTransactionStore();

    InetSocketAddress bridge$getAddress();

    InetSocketAddress bridge$getVirtualHost();

    void bridge$setVirtualHost(String host, int port);

    net.minecraft.network.chat.Component bridge$getKickReason();

    void bridge$setKickReason(net.minecraft.network.chat.Component component);

    MinecraftVersion bridge$getVersion();

    void bridge$setVersion(int version);

    Set<ResourceKey> bridge$getRegisteredChannels();

    ClientType bridge$getClientType();

    void bridge$setClientType(ClientType clientType);
}
