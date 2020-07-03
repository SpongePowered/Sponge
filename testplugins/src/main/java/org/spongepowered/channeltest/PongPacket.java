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
package org.spongepowered.channeltest;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.network.channel.ChannelBuf;
import org.spongepowered.api.network.channel.packet.Packet;

public final class PongPacket implements Packet {

    private int id;

    public PongPacket(final int id) {
        this.id = id;
    }

    private PongPacket() {
    }

    public int getId() {
        return this.id;
    }

    @Override
    public void read(final ChannelBuf buf) {
        this.id = buf.readVarInt();
    }

    @Override
    public void write(final ChannelBuf buf) {
        buf.writeVarInt(this.id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", this.id)
                .toString();
    }
}
