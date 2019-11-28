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
package org.spongepowered.common.mixin.api.mcp.network;

import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.ServerPlayNetHandler;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.network.NetworkManagerBridge;

import java.net.InetSocketAddress;

@SuppressWarnings("rawtypes")
@Mixin(NetworkManager.class)
public abstract class NetworkManagerMixin_API extends SimpleChannelInboundHandler implements PlayerConnection {

    @Shadow private INetHandler packetListener;

    @Override
    public Player getPlayer() {
        if(this.packetListener instanceof ServerPlayNetHandler) {
            return (Player) ((ServerPlayNetHandler) this.packetListener).field_147369_b;
        }
        throw new IllegalStateException("Player is not currently available");
    }

    @Override
    public int getLatency() {
        if(this.packetListener instanceof ServerPlayNetHandler) {
            return ((ServerPlayNetHandler) this.packetListener).field_147369_b.field_71138_i;
        }
        throw new IllegalStateException("Latency is not currently available");
    }


    @Override
    public InetSocketAddress getAddress() {
        return ((NetworkManagerBridge) this).bridge$getAddress();
    }

    @Override
    public InetSocketAddress getVirtualHost() {
        return ((NetworkManagerBridge) this).bridge$getVirtualHost();
    }
}
