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
import org.spongepowered.api.network.EngineConnectionState;
import org.spongepowered.api.network.channel.TimeoutException;
import org.spongepowered.common.network.SpongeEngineConnection;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * A storage for transaction id mappings.
 */
public final class TransactionStore {

    private final EngineConnection connection;

    private final ConcurrentMap<Integer, Entry> lookup = Caffeine.newBuilder()
            .expireAfterAccess(15, TimeUnit.SECONDS)
            .removalListener((RemovalListener<Integer, Entry>) (key, value, cause) -> {
                //The channel is null for few internal packets
                if (cause == RemovalCause.EXPIRED && value != null && value.getChannel() != null) {
                    final EngineConnectionState state = (EngineConnectionState) ((SpongeEngineConnection) this.connection()).connection().getPacketListener();
                    value.getChannel().handleTransactionResponse(
                        this.connection(), state, value.getData(), TransactionResult.failure(new TimeoutException()));
                }
            })
            .build().asMap();

    public static class Entry {

        private final @Nullable SpongeChannel channel;
        private final @Nullable Object data;

        public Entry(final @Nullable SpongeChannel channel, final @Nullable Object data) {
            this.channel = channel;
            this.data = data;
        }

        public @Nullable SpongeChannel getChannel() {
            return this.channel;
        }

        public @Nullable Object getData() {
            return this.data;
        }
    }

    public TransactionStore(final EngineConnection connection) {
        this.connection = connection;
    }

    /**
     * Gets the {@link EngineConnection} this transaction store belongs to.
     *
     * @return The engine connection
     */
    public EngineConnection connection() {
        return this.connection;
    }

    /**
     * Gets the next available transaction id.
     *
     * @return The transaction id
     */
    public int nextId() {
        // TODO: Hook into forge to avoid id overlap
        int id;
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        do {
            id = random.nextInt();
        } while (this.lookup.containsKey(id));
        return id;
    }

    /**
     * Adds the given {@code value} to the transaction store
     * and returns the assigned transaction id.
     *
     * @param transactionId The transaction id
     * @param channel The channel
     * @param stored The stored data
     */
    public void put(final int transactionId, final @Nullable SpongeChannel channel, final @Nullable Object stored) {
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
