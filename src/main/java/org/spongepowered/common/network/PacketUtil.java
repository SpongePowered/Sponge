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
package org.spongepowered.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.resources.ResourceLocation;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.network.EngineConnectionSide;
import org.spongepowered.api.network.channel.ChannelBuf;
import org.spongepowered.api.network.channel.packet.Packet;
import org.spongepowered.common.accessor.network.protocol.game.ClientboundAddPlayerPacketAccessor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.function.Supplier;

public final class PacketUtil {

    private static final Object UNSAFE;
    private static final MethodHandle ALLOCATE_INSTANCE;

    static {
        try {
            final Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            final Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            UNSAFE = unsafeField.get(null);
            ALLOCATE_INSTANCE = MethodHandles.lookup().findVirtual(unsafeClass, "allocateInstance", MethodType.methodType(Object.class, Class.class));
            // final jdk.internal.misc.Unsafe unsafe = jdk.internal.misc.Unsafe.getUnsafe();
        } catch (final NoSuchFieldException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException ex) {
            throw new ExceptionInInitializerError("Unable to access Unsafe instance!");
        }
    }

    public static net.minecraft.network.protocol.Packet<?> createLoginPayloadResponse(final @Nullable ChannelBuf payload, final int transactionId) {
        return new ServerboundCustomQueryPacket(transactionId, (FriendlyByteBuf) payload);
    }

    public static net.minecraft.network.protocol.Packet<?> createLoginPayloadRequest(final ResourceKey channel, final ChannelBuf payload, final int transactionId) {
        return new ClientboundCustomQueryPacket(transactionId, (ResourceLocation) (Object) channel, (FriendlyByteBuf) payload);
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

    public static ClientboundAddPlayerPacket createClientboundAddPlayerPacket(
        final int entityId,
        final UUID playerId,
        final double x,
        final double y,
        final double z,
        final byte yRot,
        final byte xRot
    ) {
        final ClientboundAddPlayerPacket packet = PacketUtil.createPacketWithoutConstructor(ClientboundAddPlayerPacket.class);
        final ClientboundAddPlayerPacketAccessor accessor = (ClientboundAddPlayerPacketAccessor) packet;
        accessor.accessor$entityId(entityId);
        accessor.accessor$playerId(playerId);
        accessor.accessor$x(x);
        accessor.accessor$y(y);
        accessor.accessor$z(z);
        accessor.accessor$yRot(yRot);
        accessor.accessor$xRot(xRot);
        return packet;
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

    /**
     * Create a packet without invoking its constructor.
     *
     * <p>This is necessary to instantiate some packets that have had their
     * constructors stripped by obfuscation, but it should never be a first
     * choice.</p>
     *
     * <p>When using this method, care must be taken to ensure
     * EVERY FIELD IS INITIALIZED. No standard constructor or instance
     * initializers will be run.</p>
     *
     * @param packet the packet to create an instance of
     * @param <P> the packet type
     * @return a new instance
     */
    @SuppressWarnings("unchecked")
    private static <P extends net.minecraft.network.protocol.Packet<?>> @NonNull P createPacketWithoutConstructor(final @NonNull Class<P> packet) {
        try {
            return (P) PacketUtil.ALLOCATE_INSTANCE.invoke(PacketUtil.UNSAFE, packet);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        } catch (final Throwable ex) {
            throw new RuntimeException("Failed to create instance of packet " + packet, ex);
        }
    }

    private PacketUtil() {
    }
}
