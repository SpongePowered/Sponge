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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.network.EngineConnectionState;
import org.spongepowered.api.network.channel.packet.Packet;
import org.spongepowered.api.network.channel.packet.PacketHandler;
import org.spongepowered.api.network.channel.packet.RequestPacket;
import org.spongepowered.api.network.channel.packet.RequestPacketHandler;
import org.spongepowered.api.network.channel.packet.ResponsePacketHandler;
import org.spongepowered.api.network.channel.packet.TransactionalPacketBinding;
import org.spongepowered.common.network.channel.ConcurrentMultimap;
import org.spongepowered.common.network.channel.SpongeChannel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

@SuppressWarnings({"unchecked", "rawtypes"})
public class SpongeTransactionalPacketBinding<P extends RequestPacket<R>, R extends Packet>
        extends SpongePacketBinding<P> implements TransactionalPacketBinding<P, R> {

    private final Map<Class<?>, RequestPacketHandler<? super P, ? extends R, ?>> requestHandlers = new HashMap<>();
    private final ConcurrentMultimap<Class<?>, ResponsePacketHandler<? super P, ? super R, ?>> responseHandlers = new ConcurrentMultimap<>();

    public SpongeTransactionalPacketBinding(final int opcode, final Class<P> requestPacketType) {
        super(opcode, requestPacketType);
    }

    public <S extends EngineConnectionState> @Nullable RequestPacketHandler<? super P, ? extends R, S> getRequestHandler(final S state) {
        return (RequestPacketHandler<? super P, ? extends R, S>) SpongeChannel.getRequestHandler(state, this.requestHandlers);
    }

    @Override
    public <S extends EngineConnectionState> TransactionalPacketBinding<P, R> setRequestHandler(final Class<S> connectioState,
            final RequestPacketHandler<? super P, ? extends R, ? super S> handler) {
        Objects.requireNonNull(connectioState, "connectionType");
        Objects.requireNonNull(handler, "handler");
        this.requestHandlers.put(connectioState, handler);
        return this;
    }

    @Override
    public TransactionalPacketBinding<P, R> setRequestHandler(final RequestPacketHandler<? super P, ? extends R, EngineConnectionState> handler) {
        Objects.requireNonNull(handler, "handler");
        return this.setRequestHandler(EngineConnectionState.class, handler);
    }

    @Override
    public <S extends EngineConnectionState> TransactionalPacketBinding<P, R> addResponseHandler(final Class<S> connectionState,
            final PacketHandler<? super R, ? super S> handler) {
        Objects.requireNonNull(handler, "handler");
        return this.addResponseHandler(connectionState, new PacketToResponseHandler<>(handler));
    }

    @Override
    public <S extends EngineConnectionState> TransactionalPacketBinding<P, R> addResponseHandler(final Class<S> connectionState,
            final ResponsePacketHandler<? super P, ? super R, ? super S> handler) {
        Objects.requireNonNull(connectionState, "connectionType");
        Objects.requireNonNull(handler, "handler");
        this.responseHandlers.modify(map -> map.put(connectionState, handler));
        return this;
    }

    @Override
    public TransactionalPacketBinding<P, R> addResponseHandler(
            final PacketHandler<? super R, EngineConnectionState> handler) {
        Objects.requireNonNull(handler, "handler");
        return this.addResponseHandler(new PacketToResponseHandler<>(handler));
    }

    @Override
    public TransactionalPacketBinding<P, R> addResponseHandler(final ResponsePacketHandler<? super P, ? super R, EngineConnectionState> handler) {
        return this.addResponseHandler(EngineConnectionState.class, handler);
    }

    public <S extends EngineConnectionState> Collection<ResponsePacketHandler<? super P, ? super R, ? super S>> getResponseHandlers(final S state) {
        return (Collection) SpongeChannel.getResponseHandlers(state, this.responseHandlers.get());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SpongeTransactionalPacketBinding.class.getSimpleName() + "[", "]")
                .add("opcode=" + this.opcode())
                .add("requestPacketType=" + this.packetType())
                .toString();
    }
}
