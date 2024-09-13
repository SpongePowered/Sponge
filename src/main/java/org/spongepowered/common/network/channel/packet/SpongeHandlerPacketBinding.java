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

import org.spongepowered.api.network.EngineConnectionState;
import org.spongepowered.api.network.channel.packet.HandlerPacketBinding;
import org.spongepowered.api.network.channel.packet.Packet;
import org.spongepowered.api.network.channel.packet.PacketBinding;
import org.spongepowered.api.network.channel.packet.PacketHandler;
import org.spongepowered.common.network.channel.ConcurrentMultimap;
import org.spongepowered.common.network.channel.SpongeChannel;

import java.util.Collection;
import java.util.Objects;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class SpongeHandlerPacketBinding<P extends Packet> extends SpongePacketBinding<P> implements HandlerPacketBinding<P> {

    private final ConcurrentMultimap<Class<?>, PacketHandler<? super P, ?>> handlers = new ConcurrentMultimap<>();

    SpongeHandlerPacketBinding(final int opcode, final Class<P> packetType) {
        super(opcode, packetType);
    }

    public <S extends EngineConnectionState> Collection<PacketHandler<? super P, ? super S>> getHandlers(final S state) {
        return (Collection) SpongeChannel.getResponseHandlers(state, this.handlers.get());
    }

    @Override
    public <S extends EngineConnectionState> PacketBinding<P> addHandler(final Class<S> connectionState,
            final PacketHandler<? super P, ? super S> handler) {
        Objects.requireNonNull(connectionState, "connectionType");
        Objects.requireNonNull(handler, "handler");
        this.handlers.modify(map -> map.put(connectionState, handler));
        return this;
    }

    @Override
    public PacketBinding<P> addHandler(final PacketHandler<? super P, EngineConnectionState> handler) {
        return this.addHandler(EngineConnectionState.class, handler);
    }

    @Override
    public <S extends EngineConnectionState> PacketBinding<P> removeHandler(final Class<S> connectionState,
            final PacketHandler<? super P, ? super S> handler) {
        Objects.requireNonNull(connectionState, "connectionType");
        Objects.requireNonNull(handler, "handler");
        this.handlers.modify(map -> map.entries()
                .removeIf(entry -> entry.getKey().isAssignableFrom(connectionState) && entry.getValue() == handler));
        return this;
    }

    @Override
    public PacketBinding<P> removeHandler(final PacketHandler<? super P, ?> handler) {
        Objects.requireNonNull(handler, "handler");
        this.handlers.modify(map -> map.entries()
                .removeIf(entry -> entry.getValue() == handler));
        return this;
    }
}
