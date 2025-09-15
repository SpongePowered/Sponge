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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.function.Predicate;

public final class AutoSaveQueue<T> {

    private final Predicate<T> predicate;
    private final LinkedHashSet<T> values;

    private final int batchInterval;
    private final int batchAmount;
    private final boolean log;

    private int counter;

    public AutoSaveQueue(final Predicate<T> predicate, final Collection<T> values, final int batchInterval, final int batchAmount, final boolean log) {
        this.predicate = predicate;
        this.values = new LinkedHashSet<>(values);
        this.batchInterval = batchInterval;
        this.batchAmount = batchAmount;
        this.log = log;
    }

    public AutoSaveQueue(final Predicate<T> predicate, final int batchInterval, final int batchAmount, final boolean log) {
        this(predicate, Collections.emptyList(), batchInterval, batchAmount, log);
    }

    public boolean log() {
        return this.log;
    }

    public boolean drain() {
        if (this.counter++ % this.batchInterval != 0) {
            return false;
        }

        int left = this.batchAmount;
        while (!this.values.isEmpty()) {
            if (this.predicate.test(this.values.removeFirst()) && --left <= 0) {
                break;
            }
        }

        return this.values.isEmpty();
    }

    public void add(final T value) {
        this.values.add(value);
    }

    public void remove(final T value) {
        this.values.remove(value);
    }
}
