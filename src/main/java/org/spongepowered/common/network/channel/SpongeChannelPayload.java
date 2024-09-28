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

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.login.custom.CustomQueryAnswerPayload;
import net.minecraft.network.protocol.login.custom.CustomQueryPayload;
import net.minecraft.resources.ResourceLocation;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;

public record SpongeChannelPayload(@Nullable Type<? extends CustomPacketPayload> type, @Nullable ResourceLocation id, @Nullable Consumer<FriendlyByteBuf> consumer) implements CustomPacketPayload, CustomQueryPayload, CustomQueryAnswerPayload {

    public static StreamCodec<FriendlyByteBuf, SpongeChannelPayload> streamCodec(final Type<? extends CustomPacketPayload> type, final int maxPayloadSize) {
        return CustomPacketPayload.codec(
            SpongeChannelPayload::write, (buffer) -> {
                final int readableBytes = buffer.readableBytes();
                if (readableBytes >= 0 && readableBytes <= maxPayloadSize) {
                    final ByteBuf payload = buffer.readBytes(readableBytes);
                    return SpongeChannelPayload.fromType(type, (b) -> b.writeBytes(payload.slice()));
                }
                throw new IllegalArgumentException("Payload may not be larger than " + maxPayloadSize + " bytes");
            });
    }

    public void write(final FriendlyByteBuf buf) {
        this.consumer.accept(buf);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return this.type;
    }

    @Override
    public ResourceLocation id() {
        return this.id;
    }

    public static SpongeChannelPayload fromType(final Type<? extends CustomPacketPayload> type, Consumer<FriendlyByteBuf> consumer) {
        return new SpongeChannelPayload(type, type.id(), consumer);
    }

    public static SpongeChannelPayload fromId(final ResourceLocation id, Consumer<FriendlyByteBuf> consumer) {
        return new SpongeChannelPayload(null, id, consumer);
    }

    public static SpongeChannelPayload bufferOnly(@Nullable Consumer<FriendlyByteBuf> consumer) {
        return new SpongeChannelPayload(null, null, consumer);
    }
}
