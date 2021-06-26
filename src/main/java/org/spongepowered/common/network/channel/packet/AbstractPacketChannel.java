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
package org.spongepowered.common.network.channel.packet;

import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.api.network.channel.ChannelBuf;
import org.spongepowered.api.network.channel.ChannelException;
import org.spongepowered.api.network.channel.ChannelIOException;
import org.spongepowered.api.network.channel.packet.FixedTransactionalPacketBinding;
import org.spongepowered.api.network.channel.packet.HandlerPacketBinding;
import org.spongepowered.api.network.channel.packet.Packet;
import org.spongepowered.api.network.channel.packet.PacketBinding;
import org.spongepowered.api.network.channel.packet.PacketHandler;
import org.spongepowered.api.network.channel.packet.RequestPacket;
import org.spongepowered.api.network.channel.packet.ResponsePacketHandler;
import org.spongepowered.api.network.channel.packet.TransactionalPacketBinding;
import org.spongepowered.api.network.channel.packet.TransactionalPacketRegistry;
import org.spongepowered.common.network.channel.ChannelBuffers;
import org.spongepowered.common.network.channel.SpongeChannel;
import org.spongepowered.common.network.channel.SpongeChannelManager;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractPacketChannel extends SpongeChannel implements TransactionalPacketRegistry {

    protected final Map<Class<?>, PacketBinding<?>> byType = new ConcurrentHashMap<>();
    protected final Map<Integer, PacketBinding<?>> byOpcode = new ConcurrentHashMap<>();

    public AbstractPacketChannel(final int type, final ResourceKey key, final SpongeChannelManager registry) {
        super(type, key, registry);
    }

    @Override
    public Collection<PacketBinding<?>> bindings() {
        return ImmutableList.copyOf(this.byOpcode.values());
    }

    static final class TransactionData<P extends RequestPacket<R>, R extends Packet> {

        final P request;
        final SpongeTransactionalPacketBinding<P, R> binding;
        final CompletableFuture<?> future;
        final @Nullable Consumer<R> success;

        TransactionData(final P request, final SpongeTransactionalPacketBinding<P, R> binding,
                final @Nullable Consumer<R> success,
                final CompletableFuture<?> future) {
            this.request = request;
            this.binding = binding;
            this.success = success;
            this.future = future;
        }
    }

    protected void encodePayload(final ChannelBuf payload, final Packet packet) {
        final ChannelBuf packetContent = this.manager().getBufferAllocator().buffer();
        try {
            this.encodePayloadUnsafe(packetContent, packet);
            ChannelBuffers.write(payload, packetContent);
        } catch (final Throwable ex) {
            ChannelBuffers.release(payload);
            throw ex;
        } finally {
            ChannelBuffers.release(packetContent);
        }
    }

    protected void encodePayloadUnsafe(final ChannelBuf payload, final Packet packet) {
        try {
            packet.write(payload);
        } catch (final Throwable ex) {
            throw new ChannelIOException("Failed to encode " + packet.getClass(), ex);
        }
    }

    protected <P extends Packet> P decodePayload(final Supplier<P> packetSupplier, final ChannelBuf payload) {
        final P packet = packetSupplier.get();

        try {
            packet.read(payload.slice());
        } catch (final Exception ex) {
            throw new ChannelIOException("Failed to decode " + packet.getClass(), ex);
        }

        return packet;
    }

    protected SpongePacketBinding<Packet> requireBinding(final int opcode) {
        final SpongePacketBinding<Packet> binding = (SpongePacketBinding<Packet>) this.byOpcode.get(opcode);
        if (binding == null) {
            throw new ChannelException("Unknown opcode " + opcode + " for channel " + this.key());
        }
        return binding;
    }

    protected SpongePacketBinding<Packet> requireBinding(final Class<? extends Packet> packet) {
        final SpongePacketBinding<Packet> binding = (SpongePacketBinding<Packet>) this.byType.get(packet);
        if (binding == null) {
            throw new ChannelException("Unknown packet type " + packet + " for channel " + this.key());
        }
        return binding;
    }

    /**
     * Checks whether the given packet class is valid.
     *
     * @param packetClass The packet class
     */
    protected void validatePacketClass(final Class<?> packetClass) {
        // Check for zero arg constructor
        try {
            packetClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("A packet class is required to have a zero arg constructor.");
        }
    }

    @Override
    public <P extends Packet> HandlerPacketBinding<P> register(final Class<P> packetClass, final int packetOpcode) {
        this.checkPossibleRegistration(packetClass, packetOpcode);
        final SpongeHandlerPacketBinding<P> binding = new SpongeHandlerPacketBinding<>(packetOpcode, packetClass);
        this.byType.put(packetClass, binding);
        this.byOpcode.put(packetOpcode, binding);
        return binding;
    }

    @Override
    public <P extends RequestPacket<R>, R extends Packet> FixedTransactionalPacketBinding<P, R> registerTransactional(
            final Class<P> requestPacketType, final Class<R> responsePacketType, final int opcode) {
        Objects.requireNonNull(requestPacketType, "requestPacketType");
        Objects.requireNonNull(responsePacketType, "responsePacketType");
        this.checkPossibleRegistration(requestPacketType, opcode);
        final SpongeFixedTransactionalPacketBinding<P, R> binding = new SpongeFixedTransactionalPacketBinding<>(
                opcode, requestPacketType, responsePacketType);
        this.byType.put(requestPacketType, binding);
        this.byOpcode.put(opcode, binding);
        return binding;
    }

    @Override
    public <P extends RequestPacket<R>, R extends Packet> TransactionalPacketBinding<P, R> registerTransactional(
            final Class<P> requestPacketType, final int opcode) {
        Objects.requireNonNull(requestPacketType, "requestPacketType");
        this.checkPossibleRegistration(requestPacketType, opcode);
        final SpongeTransactionalPacketBinding<P, R> binding = new SpongeTransactionalPacketBinding<>(
                opcode, requestPacketType);
        this.byType.put(requestPacketType, binding);
        this.byOpcode.put(opcode, binding);
        return binding;
    }

    private void checkPossibleRegistration(final Class<?> packetClass, final int opcode) {
        Objects.requireNonNull(packetClass, "packetClass");
        this.validatePacketClass(packetClass);
        if (this.byType.containsKey(packetClass)) {
            throw new IllegalArgumentException("The packet type \"" + packetClass.getName() + "\" is already registered.");
        }
        if (this.byOpcode.containsKey(opcode)) {
            throw new IllegalArgumentException("The opcode \"" + opcode + "\" is already in use.");
        }
    }

    @Override
    public <P extends RequestPacket<R>, R extends Packet> Optional<TransactionalPacketBinding<P, R>> transactionalBinding(
            final Class<P> requestPacketType) {
        Objects.requireNonNull(requestPacketType, "requestPacketType");
        return Optional.ofNullable((TransactionalPacketBinding<P, R>) this.byType.get(requestPacketType));
    }

    @Override
    public <M extends Packet> Optional<PacketBinding<M>> binding(final Class<M> packetClass) {
        Objects.requireNonNull(packetClass, "packetClass");
        return Optional.ofNullable((PacketBinding<M>) this.byType.get(packetClass));
    }

    @Override
    public Optional<PacketBinding<?>> binding(final int opcode) {
        return Optional.ofNullable(this.byOpcode.get(opcode));
    }

    protected <P extends RequestPacket<R>, R extends Packet, C extends EngineConnection> void handleResponse(final C connection,
            final TransactionalPacketBinding<P, R> binding, final P request, final R response) {
        for (final ResponsePacketHandler<? super P, ? super R, ? super C> handler :
                ((SpongeTransactionalPacketBinding<P, R>) binding).getResponseHandlers(connection)) {
            try {
                handler.handleResponse(response, request, connection);
            } catch (final Throwable t) {
                this.handleException(connection, new ChannelException("Failed to handle packet", t), null);
            }
        }
    }

    protected <P extends RequestPacket<R>, R extends Packet, C extends EngineConnection> void handleResponseFailure(final C connection,
            final TransactionalPacketBinding<P, R> binding, final P request, final ChannelException response) {
        for (final ResponsePacketHandler<? super P, ? super R, ? super C> handler :
                ((SpongeTransactionalPacketBinding<P, R>) binding).getResponseHandlers(connection)) {
            try {
                handler.handleFailure(response, request, connection);
            } catch (final Throwable t) {
                this.handleException(connection, new ChannelException("Failed to handle packet failure", t), null);
            }
        }
    }

    protected <P extends Packet, C extends EngineConnection> void handle(final C connection, final HandlerPacketBinding<P> binding, final P packet) {
        for (final PacketHandler<? super P, ? super C> handler : ((SpongeHandlerPacketBinding<P>) binding).getHandlers(connection)) {
            try {
                handler.handle(packet, connection);
            } catch (final Throwable t) {
                this.handleException(connection, new ChannelException("Failed to handle packet", t), null);
            }
        }
    }
}
