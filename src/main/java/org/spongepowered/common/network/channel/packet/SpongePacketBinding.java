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

import org.spongepowered.api.network.channel.packet.Packet;
import org.spongepowered.api.network.channel.packet.PacketBinding;
import org.spongepowered.common.network.PacketUtil;

import java.util.StringJoiner;
import java.util.function.Supplier;

public abstract class SpongePacketBinding<P extends Packet> implements PacketBinding<P> {

    private final int opcode;
    private final Class<P> packetType;
    private final Supplier<P> constructor;

    public SpongePacketBinding(final int opcode, final Class<P> packetType) {
        this.opcode = opcode;
        this.packetType = packetType;
        this.constructor = PacketUtil.getConstructor(packetType);
    }

    @Override
    public int opcode() {
        return this.opcode;
    }

    @Override
    public Class<P> packetType() {
        return this.packetType;
    }

    public Supplier<P> getPacketConstructor() {
        return this.constructor;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SpongePacketBinding.class.getSimpleName() + "[", "]")
                .add("opcode=" + this.opcode)
                .add("packetType=" + this.packetType.getName())
                .toString();
    }
}
