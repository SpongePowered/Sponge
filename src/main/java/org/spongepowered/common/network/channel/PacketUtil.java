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
package org.spongepowered.common.network.channel;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.network.EngineConnectionSide;
import org.spongepowered.api.network.channel.ChannelBuf;
import org.spongepowered.api.network.channel.packet.Packet;
import org.spongepowered.common.accessor.network.protocol.game.ClientboundCustomPayloadPacketAccessor;
import org.spongepowered.common.accessor.network.protocol.game.ServerboundCustomPayloadPacketAccessor;
import org.spongepowered.common.accessor.network.protocol.login.ClientboundCustomQueryPacketAccessor;
import org.spongepowered.common.accessor.network.protocol.login.ServerboundCustomQueryPacketAccessor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.resources.ResourceLocation;

public final class PacketUtil {

    public static net.minecraft.network.protocol.Packet<?> createLoginPayloadResponse(final @Nullable ChannelBuf payload, final int transactionId) {
        return new ServerboundCustomQueryPacket(transactionId, (FriendlyByteBuf) payload);
    }

    public static net.minecraft.network.protocol.Packet<?> createLoginPayloadRequest(final ResourceKey channel, final ChannelBuf payload, final int transactionId) {
        final ClientboundCustomQueryPacket packet = new ClientboundCustomQueryPacket();
        final ClientboundCustomQueryPacketAccessor accessor = (ClientboundCustomQueryPacketAccessor) packet;
        accessor.accessor$identifier((ResourceLocation) (Object) channel);
        accessor.accessor$transactionId(transactionId);
        accessor.accessor$data((FriendlyByteBuf) payload);
        return packet;
    }

    public static net.minecraft.network.protocol.Packet<?> createPlayPayload(final ResourceKey channel, final ChannelBuf payload, final EngineConnectionSide<?> side) {
        if (side == EngineConnectionSide.CLIENT) {
            return new ServerboundCustomPayloadPacket((ResourceLocation) (Object) channel, (FriendlyByteBuf) payload);
        } else if (side == EngineConnectionSide.SERVER) {
            return new ClientboundCustomPayloadPacket((ResourceLocation) (Object) channel, (FriendlyByteBuf) payload);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    // TODO: Use Lmbda instead?
    public static <P extends Packet> Supplier<P> getConstructor(final Class<P> packetClass) {
        final Constructor<P> constructor;
        try {
            constructor = packetClass.getDeclaredConstructor();
            constructor.setAccessible(true);
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
        return () -> {
            try {
                return (P) constructor.newInstance();
            } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        };
    }

    private PacketUtil() {
    }
}
