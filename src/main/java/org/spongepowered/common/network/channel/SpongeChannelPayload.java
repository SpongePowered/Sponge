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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SpongeChannelPayload(Type<? extends CustomPacketPayload> type, FriendlyByteBuf payload) implements CustomPacketPayload {

    public static StreamCodec<FriendlyByteBuf, SpongeChannelPayload> streamCodec(final Type<? extends CustomPacketPayload> type, final int maxPayloadSize) {
        return CustomPacketPayload.codec(
                SpongeChannelPayload::write, (b) -> {
                    int readableBytes = b.readableBytes();
                    if (readableBytes >= 0 && readableBytes <= maxPayloadSize) {
                        return new SpongeChannelPayload(type, new FriendlyByteBuf(b.readBytes(b.readableBytes())));
                    }
                    throw new IllegalArgumentException("Payload may not be larger than " + maxPayloadSize + " bytes");
                });
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeBytes(this.payload);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return this.type;
    }
}
