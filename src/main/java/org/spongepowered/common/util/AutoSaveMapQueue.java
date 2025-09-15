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
package org.spongepowered.common.util;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.launch.config.common.AutoSaveOptions;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiPredicate;

public final class AutoSaveMapQueue<T> {

    private final HashMap<T, AutoSaveQueueKey> values;
    private final Map<AutoSaveQueueKey, AutoSaveQueue<T>> queues;

    private final BiPredicate<T, Boolean> predicate;

    public AutoSaveMapQueue(final BiPredicate<T, Boolean> predicate) {
        this.values = new HashMap<>();
        this.queues = new LinkedHashMap<>();

        this.predicate = predicate;
    }

    public void drain() {
        this.queues.values().removeIf(AutoSaveQueue::drain);
    }

    public void add(final AutoSaveOptions options, final T value) {
        final AutoSaveQueueKey key = new AutoSaveQueueKey(options);
        if (this.values.putIfAbsent(value, key) != null) {
            return;
        }

        final AutoSaveQueue<T> queue = this.queues.computeIfAbsent(key, k ->
            new AutoSaveQueue<>(v -> {
                this.values.remove(v);

                return this.predicate.test(v, k.log());
            }, k.batchInterval(), Math.max(1, k.batchAmount()), k.log()));

        queue.add(value);
    }

    public void remove(final T value) {
        final @Nullable AutoSaveQueueKey key = this.values.remove(value);
        if (key != null) {
            this.queues.get(key).remove(value);
        }
    }

    private record AutoSaveQueueKey(int batchInterval, int batchAmount, boolean log) {

        AutoSaveQueueKey(final AutoSaveOptions options) {
            this(options.batchInterval, options.batchAmount, options.log);
        }
    }
}
