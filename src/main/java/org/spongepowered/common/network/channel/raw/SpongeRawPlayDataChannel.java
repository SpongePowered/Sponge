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
package org.spongepowered.common.network.channel.raw;

import net.minecraft.network.protocol.Packet;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.api.network.EngineConnectionState;
import org.spongepowered.api.network.channel.ChannelBuf;
import org.spongepowered.api.network.channel.raw.play.RawPlayDataChannel;
import org.spongepowered.api.network.channel.raw.play.RawPlayDataHandler;
import org.spongepowered.common.network.PacketUtil;
import org.spongepowered.common.network.SpongeEngineConnection;
import org.spongepowered.common.network.channel.ConcurrentMultimap;
import org.spongepowered.common.network.channel.ConnectionUtil;
import org.spongepowered.common.network.channel.PacketSender;
import org.spongepowered.common.network.channel.SpongeChannel;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@SuppressWarnings({"unchecked", "rawtypes"})
public class SpongeRawPlayDataChannel implements RawPlayDataChannel {

    private final ConcurrentMultimap<Class<?>, RawPlayDataHandler<?>> handlers = new ConcurrentMultimap<>();
    private final SpongeRawDataChannel parent;

    public SpongeRawPlayDataChannel(final SpongeRawDataChannel parent) {
        this.parent = parent;
    }

    @Override
    public SpongeRawDataChannel parent() {
        return this.parent;
    }

    @Override
    public boolean isSupportedBy(final EngineConnection connection) {
        Objects.requireNonNull(connection, "connection");
        return ConnectionUtil.getRegisteredChannels(connection).contains(this.parent.key());
    }

    @Override
    public void addHandler(final RawPlayDataHandler<EngineConnectionState> handler) {
        this.addHandler(EngineConnectionState.class, handler);
    }

    @Override
    public <S extends EngineConnectionState> void addHandler(final Class<S> connectionState, final RawPlayDataHandler<? super S> handler) {
        Objects.requireNonNull(connectionState, "connectionType");
        Objects.requireNonNull(handler, "handler");
        this.handlers.modify(map -> map.put(connectionState, handler));
    }

    @Override
    public void removeHandler(final RawPlayDataHandler<?> handler) {
        Objects.requireNonNull(handler, "handler");
        this.handlers.modify(map -> map.entries().removeIf(entry -> entry.getValue() == handler));
    }

    @Override
    public <S extends EngineConnectionState> void removeHandler(final Class<S> connectionState, final RawPlayDataHandler<? super S> handler) {
        Objects.requireNonNull(connectionState, "connectionType");
        Objects.requireNonNull(handler, "handler");
        this.handlers.modify(map -> map.entries().removeIf(entry -> entry.getKey().isAssignableFrom(connectionState) && entry.getValue() == handler));
    }

    @Override
    public CompletableFuture<Void> sendTo(final EngineConnection connection, final Consumer<ChannelBuf> consumer) {
        Objects.requireNonNull(connection, "connection");
        Objects.requireNonNull(consumer, "payload");

        ConnectionUtil.checkPlayPhase(connection);

        final EngineConnectionState state = (EngineConnectionState) ((SpongeEngineConnection) connection).connection().getPacketListener();

        final CompletableFuture<Void> future = new CompletableFuture<>();
        final ChannelBuf payload;
        try {
            payload = this.parent.encodePayload(consumer);
        } catch (final Throwable ex) {
            this.parent.handleException(connection, state, ex, future);
            return future;
        }

        final Packet<?> mcPacket = PacketUtil.createPlayPayload(this.parent.payloadType(), payload, connection.side());
        PacketSender.sendTo(connection, mcPacket, future);
        return future;
    }

    private <S extends EngineConnectionState> Collection<RawPlayDataHandler<? super S>> getHandlers(final S state) {
        return (Collection) SpongeChannel.getResponseHandlers(state, this.handlers.get());
    }

    <S extends EngineConnectionState> void handlePayload(final S state, final ChannelBuf payload) {
        for (final RawPlayDataHandler<? super S> handler : this.getHandlers(state)) {
            handler.handlePayload(payload.slice(), state);
        }
    }
}
