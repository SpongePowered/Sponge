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

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.api.network.channel.TimeoutException;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * A storage for transaction id mappings.
 */
public final class TransactionStore {

    private final Supplier<EngineConnection> connection;

    private final AtomicInteger counter = new AtomicInteger();
    private final ConcurrentMap<Integer, Entry> lookup = Caffeine.newBuilder()
            .expireAfterAccess(15, TimeUnit.SECONDS)
            .removalListener((RemovalListener<Integer, Entry>) (key, value, cause) -> {
                if (cause == RemovalCause.EXPIRED && value != null) {
                    value.getChannel().handleTransactionResponse(
                            getConnection(), value.getData(), TransactionResult.failure(new TimeoutException()));
                }
            })
            .build().asMap();

    public static class Entry {

        private final SpongeChannel channel;
        private final Object data;

        public Entry(final SpongeChannel channel, final Object data) {
            this.channel = channel;
            this.data = data;
        }

        public SpongeChannel getChannel() {
            return this.channel;
        }

        public Object getData() {
            return this.data;
        }
    }

    public TransactionStore(final Supplier<EngineConnection> connection) {
        this.connection = connection;
    }

    /**
     * Gets the {@link EngineConnection} this transaction store belongs to.
     *
     * @return The engine connection
     */
    public EngineConnection getConnection() {
        return this.connection.get();
    }

    /**
     * Gets the next available transaction id.
     *
     * @return The transaction id
     */
    public int nextId() {
        // TODO: Hook into forge to avoid id overlap
        return this.counter.getAndIncrement();
    }

    /**
     * Adds the given {@code value} to the transaction store
     * and returns the assigned transaction id.
     *
     * @param transactionId The transaction id
     * @param channel The channel
     * @param stored The stored data
     */
    public void put(final int transactionId, final SpongeChannel channel, final Object stored) {
        this.lookup.put(transactionId, new Entry(channel, stored));
    }

    /**
     * Gets the value that is assigned to the given
     * {@code transactionId}, if it exists. The mapping will be removed.
     *
     * @param transactionId The transaction id
     * @return The stored value
     */
    public @Nullable Entry remove(final int transactionId) {
        return this.lookup.remove(transactionId);
    }

    /**
     * Whether the transaction store is empty.
     *
     * @return Is empty
     */
    public boolean isEmpty() {
        return this.lookup.isEmpty();
    }
}
