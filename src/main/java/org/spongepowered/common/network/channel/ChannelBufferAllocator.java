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

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.spongepowered.api.network.channel.ChannelBuf;

public final class ChannelBufferAllocator {

    public static final ChannelBufferAllocator UNPOOLED = new ChannelBufferAllocator(UnpooledByteBufAllocator.DEFAULT);

    public static final ChannelBufferAllocator POOLED = new ChannelBufferAllocator(PooledByteBufAllocator.DEFAULT);

    private final ByteBufAllocator allocator;

    public ChannelBufferAllocator(final ByteBufAllocator allocator) {
        this.allocator = allocator;
    }

    public ChannelBuf buffer() {
        return ChannelBuffers.wrap(this.allocator.buffer());
    }

    public ChannelBuf buffer(final int initialCapacity) {
        return ChannelBuffers.wrap(this.allocator.buffer(initialCapacity));
    }

    public ChannelBuf heapBuffer() {
        return ChannelBuffers.wrap(this.allocator.heapBuffer());
    }

    public ChannelBuf heapBuffer(final int initialCapacity) {
        return ChannelBuffers.wrap(this.allocator.heapBuffer(initialCapacity));
    }

    public ChannelBuf directBuffer() {
        return ChannelBuffers.wrap(this.allocator.directBuffer());
    }

    public ChannelBuf directBuffer(final int initialCapacity) {
        return ChannelBuffers.wrap(this.allocator.directBuffer(initialCapacity));
    }
}
