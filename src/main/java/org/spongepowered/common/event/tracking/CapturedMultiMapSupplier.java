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
package org.spongepowered.common.event.tracking;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public abstract class CapturedMultiMapSupplier<K, V> implements Supplier<ListMultimap<K, V>> {

    @Nullable private ListMultimap<K, V> captured;

    CapturedMultiMapSupplier() {
    }

    @Override
    public ListMultimap<K, V> get() {
        if (this.captured == null) {
            this.captured = ArrayListMultimap.create();
        }
        return this.captured;
    }

    public final boolean isEmpty() {
        return this.captured == null || this.captured.isEmpty();
    }

    public final void ifPresentAndNotEmpty(Consumer<ListMultimap<K, V>> consumer) {
        if (this.captured != null && !this.captured.isEmpty()) {
            consumer.accept(this.captured);
        }
    }

    public final void ifPresentAndNotEmpty(BiConsumer<K, List<V>> biConsumer) {
        if (this.captured != null && !this.captured.isEmpty()) {
            for (K key : this.captured.asMap().keySet()) {
                biConsumer.accept(key, this.captured.get(key)); // Note that this will cause the consumer to be called for each key
            }
        }
    }

    public final void ifPresentAndNotEmpty(K key, Consumer<List<V>> consumer) {
        if (this.captured != null && !this.captured.isEmpty()) {
            if (this.captured.containsKey(key)) {
                consumer.accept(this.captured.get(key));
            }
        }
    }

    public final ListMultimap<K, V> orElse(ListMultimap<K, V> list) {
        return this.captured == null ? list : this.captured;
    }

    public final Stream<V> stream(K key) {
        // authors note: Multimap#get(K) returns an empty collection if there is no mapping.
        return this.captured == null ? Stream.empty() : this.captured.containsKey(key) ? this.captured.get(key).stream() : Stream.empty();
    }

    @Nullable
    public final <U> U map(K key, Function<List<V>, ? extends U> function) {
        return this.captured == null ? null : function.apply(this.captured.get(key));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.captured);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final CapturedMultiMapSupplier<?, ?> other = (CapturedMultiMapSupplier<?, ?>) obj;
        return Objects.equals(this.captured, other.captured);
    }

    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("Captured", this.captured == null ? 0 : this.captured.size())
                .toString();
    }
}
