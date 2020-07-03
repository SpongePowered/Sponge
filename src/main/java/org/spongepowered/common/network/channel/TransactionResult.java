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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.network.channel.ChannelBuf;
import org.spongepowered.api.network.channel.ChannelException;

public final class TransactionResult {

    public static TransactionResult success(final ChannelBuf payload) {
        return new TransactionResult(payload, null);
    }

    public static TransactionResult failure(final ChannelException cause) {
        return new TransactionResult(null, cause);
    }

    private final @Nullable ChannelBuf payload;
    private final @Nullable ChannelException cause;

    private TransactionResult(final ChannelBuf payload, final ChannelException cause) {
        this.payload = payload;
        this.cause = cause;
    }

    public boolean isSuccess() {
        return this.cause == null;
    }

    public @Nullable ChannelException getCause() {
        return this.cause;
    }

    public @Nullable ChannelBuf getPayload() {
        return this.payload;
    }
}
