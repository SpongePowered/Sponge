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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import java.util.function.Consumer;

public final class ConcurrentMultimap<K, V> {

    private final Multimap<K, V> multimap = HashMultimap.create();
    private volatile Multimap<K, V> immutable = null;

    public void modify(final Consumer<Multimap<K, V>> consumer) {
        synchronized (this.multimap) {
            consumer.accept(this.multimap);
            this.immutable = null;
        }
    }

    public Multimap<K, V> get() {
        Multimap<K, V> immutable = this.immutable;
        if (immutable != null) {
            return immutable;
        }
        synchronized (this.multimap) {
            immutable = this.immutable;
            if (immutable != null) {
                return immutable;
            }
            immutable = ImmutableMultimap.copyOf(this.multimap);
            this.immutable = immutable;
            return immutable;
        }
    }
}
