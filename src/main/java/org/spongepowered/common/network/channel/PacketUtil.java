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

import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.client.CCustomPayloadLoginPacket;
import net.minecraft.network.login.server.SCustomPayloadLoginPacket;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.util.ResourceLocation;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.network.EngineConnectionSide;
import org.spongepowered.api.network.channel.ChannelBuf;
import org.spongepowered.api.network.channel.packet.Packet;
import org.spongepowered.common.accessor.network.login.client.CCustomPayloadLoginPacketAccessor;
import org.spongepowered.common.accessor.network.login.server.SCustomPayloadLoginPacketAccessor;
import org.spongepowered.common.accessor.network.play.client.CCustomPayloadPacketAccessor;
import org.spongepowered.common.accessor.network.play.server.SCustomPayloadPlayPacketAccessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

public final class PacketUtil {

    public static IPacket<?> createLoginPayloadResponse(final @Nullable ChannelBuf payload, final int transactionId) {
        final CCustomPayloadLoginPacket packet = new CCustomPayloadLoginPacket();
        final CCustomPayloadLoginPacketAccessor accessor = (CCustomPayloadLoginPacketAccessor) packet;
        accessor.accessor$setTransactionId(transactionId);
        accessor.accessor$setPayload((PacketBuffer) payload);
        return packet;
    }

    public static IPacket<?> createLoginPayloadRequest(final ResourceKey channel, final ChannelBuf payload, final int transactionId) {
        final SCustomPayloadLoginPacket packet = new SCustomPayloadLoginPacket();
        final SCustomPayloadLoginPacketAccessor accessor = (SCustomPayloadLoginPacketAccessor) packet;
        accessor.accessor$setChannel((ResourceLocation) (Object) channel);
        accessor.accessor$setTransactionId(transactionId);
        accessor.accessor$setPayload((PacketBuffer) payload);
        return packet;
    }

    public static IPacket<?> createPlayPayload(final ResourceKey channel, final ChannelBuf payload, final EngineConnectionSide<?> side) {
        if (side == EngineConnectionSide.CLIENT) {
            final CCustomPayloadPacketAccessor packet = (CCustomPayloadPacketAccessor) new CCustomPayloadPacket();
            packet.accessor$setChannel((ResourceLocation) (Object) channel);
            packet.accessor$setPayload((PacketBuffer) payload);
            return (IPacket<?>) packet;
        } else if (side == EngineConnectionSide.SERVER) {
            final SCustomPayloadPlayPacketAccessor packet = (SCustomPayloadPlayPacketAccessor) new SCustomPayloadPlayPacket();
            packet.accessor$setChannel((ResourceLocation) (Object) channel);
            packet.accessor$setPayload((PacketBuffer) payload);
            return (IPacket<?>) packet;
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
