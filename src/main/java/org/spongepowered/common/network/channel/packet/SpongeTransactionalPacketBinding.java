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

import com.google.common.base.MoreObjects;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.api.network.EngineConnectionSide;
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

@SuppressWarnings({"unchecked", "rawtypes"})
public class SpongeTransactionalPacketBinding<P extends RequestPacket<R>, R extends Packet>
        extends SpongePacketBinding<P> implements TransactionalPacketBinding<P, R> {

    private final Map<Class<?>, RequestPacketHandler<? super P, ? extends R, ?>> requestHandlers = new HashMap<>();
    private final ConcurrentMultimap<Class<?>, ResponsePacketHandler<? super P, ? super R, ?>> responseHandlers = new ConcurrentMultimap<>();

    public SpongeTransactionalPacketBinding(final int opcode, final Class<P> requestPacketType) {
        super(opcode, requestPacketType);
    }

    public <C extends EngineConnection> @Nullable RequestPacketHandler<? super P, ? extends R, C> getRequestHandler(final C connection) {
        return (RequestPacketHandler<? super P, ? extends R, C>) SpongeChannel.getRequestHandler(connection, this.requestHandlers);
    }

    @Override
    public <C extends EngineConnection> TransactionalPacketBinding<P, R> setRequestHandler(
            final EngineConnectionSide<C> side, final RequestPacketHandler<? super P, ? extends R, ? super C> handler) {
        Objects.requireNonNull(side, "side");
        return this.setRequestHandler(SpongeChannel.getConnectionClass(side), handler);
    }

    @Override
    public <C extends EngineConnection> TransactionalPacketBinding<P, R> setRequestHandler(final Class<C> connectionType,
            final RequestPacketHandler<? super P, ? extends R, ? super C> handler) {
        Objects.requireNonNull(connectionType, "connectionType");
        Objects.requireNonNull(handler, "handler");
        this.requestHandlers.put(connectionType, handler);
        return this;
    }

    @Override
    public TransactionalPacketBinding<P, R> setRequestHandler(final RequestPacketHandler<? super P, ? extends R, EngineConnection> handler) {
        Objects.requireNonNull(handler, "handler");
        return this.setRequestHandler(EngineConnection.class, handler);
    }

    @Override
    public <C extends EngineConnection> TransactionalPacketBinding<P, R> addResponseHandler(
            final EngineConnectionSide<C> side, final PacketHandler<? super R, ? super C> handler) {
        Objects.requireNonNull(side, "side");
        return this.addResponseHandler(SpongeChannel.getConnectionClass(side), handler);
    }

    @Override
    public <C extends EngineConnection> TransactionalPacketBinding<P, R> addResponseHandler(final Class<C> connectionType,
            final PacketHandler<? super R, ? super C> handler) {
        Objects.requireNonNull(handler, "handler");
        return this.addResponseHandler(connectionType, new PacketToResponseHandler<>(handler));
    }

    @Override
    public <C extends EngineConnection> TransactionalPacketBinding<P, R> addResponseHandler(
            final EngineConnectionSide<C> side, final ResponsePacketHandler<? super P, ? super R, ? super C> handler) {
        Objects.requireNonNull(side, "side");
        Objects.requireNonNull(handler, "handler");
        return this.addResponseHandler(SpongeChannel.getConnectionClass(side), handler);
    }

    @Override
    public <C extends EngineConnection> TransactionalPacketBinding<P, R> addResponseHandler(final Class<C> connectionType,
            final ResponsePacketHandler<? super P, ? super R, ? super C> handler) {
        Objects.requireNonNull(connectionType, "connectionType");
        Objects.requireNonNull(handler, "handler");
        this.responseHandlers.modify(map -> map.put(connectionType, handler));
        return this;
    }

    @Override
    public TransactionalPacketBinding<P, R> addResponseHandler(
            final PacketHandler<? super R, EngineConnection> handler) {
        Objects.requireNonNull(handler, "handler");
        return this.addResponseHandler(new PacketToResponseHandler<>(handler));
    }

    @Override
    public TransactionalPacketBinding<P, R> addResponseHandler(final ResponsePacketHandler<? super P, ? super R, EngineConnection> handler) {
        return this.addResponseHandler(EngineConnection.class, handler);
    }

    public <C extends EngineConnection> Collection<ResponsePacketHandler<? super P, ? super R, ? super C>> getResponseHandlers(final C connection) {
        return (Collection) SpongeChannel.getResponseHandlers(connection, this.responseHandlers.get());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("opcode", this.getOpcode())
                .add("requestPacketType", this.getPacketType())
                .toString();
    }
}
