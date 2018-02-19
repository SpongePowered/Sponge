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
package org.spongepowered.common.event.tracking.context;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public abstract class CapturedMultiMapSupplier<K, V> implements Supplier<ArrayListMultimap<K, V>> {

    @Nullable private ArrayListMultimap<K, V> captured;

    protected CapturedMultiMapSupplier() {
    }

    @Override
    public ArrayListMultimap<K, V> get() {
        if (this.captured == null) {
            this.captured = ArrayListMultimap.create();
        }
        return this.captured;
    }

    /**
     * Returns {@code true} if there are no captured objects.
     * 
     * @return {@code true} if empty
     */
    public final boolean isEmpty() {
        return this.captured == null || this.captured.isEmpty();
    }

    /**
     * If not empty, activates the consumer then clears all captures.
     * 
     * @param consumer The consumer to activate
     */
    public final void acceptAndClearIfNotEmpty(Consumer<ListMultimap<K, V>> consumer) {
        if (!this.isEmpty()) {
            consumer.accept(this.captured);
            this.captured.clear();
        }
    }

    /**
     * If not empty, activates the {@link BiConsumer} with captures.
     * 
     * @param biConsumer The consumer to activate
     */
    public final void acceptIfNotEmpty(BiConsumer<K, List<V>> biConsumer) {
        if (!this.isEmpty()) {
            for (K key : this.captured.asMap().keySet()) {
                final List<V> values = this.captured.get(key);
                if (!values.isEmpty()) {
                    // Note that this will cause the consumer to be called for each key
                    biConsumer.accept(key, values);
                }
            }
        }
    }

    /**
     * If not empty, activates the consumer with the captured
     * {@link ListMultimap}.
     * 
     * @param consumer The consumer to activate
     */
    public final void acceptIfNotEmpty(Consumer<ListMultimap<K, V>> consumer) {
        if (!this.isEmpty()) {
            consumer.accept(this.captured);
        }
    }

    /**
     * If not empty and key is present, removes the key then activates
     * the consumer with the captured {@link ListMultimap}.
     * 
     * @param key The key to process and remove
     * @param consumer The consumer to activate
     */
    public final void acceptAndRemoveIfPresent(K key, Consumer<List<V>> consumer) {
        if (!this.isEmpty()) {
            final List<V> values = this.captured.removeAll(key);
            if (!values.isEmpty()) {
                consumer.accept(values);
            }
        }
    }


    public final void acceptAndRemoveOrNewList(K key, Consumer<List<V>> consumer) {
        if (this.isEmpty()) {
            consumer.accept(new ArrayList<>());
            return;
        }
        final List<V> values = this.captured.removeAll(key);
        if (!values.isEmpty()) {
            consumer.accept(values);
        } else {
            consumer.accept(new ArrayList<>());
        }
    }

    /**
     * If not empty, removes all values associated with key.
     * 
     * Note: If you require the list of removals, use
     * {@link #acceptAndRemoveIfPresent}
     * 
     * @param key The key to remove
     */
    public final void removeAllIfNotEmpty(K key) {
        if (this.isEmpty()) {
            return;
        }

        this.captured.removeAll(key);
    }

    /**
     * If not empty, returns the captured {@link ListMultimap}.
     * Otherwise, this will return the passed list.
     * 
     * @param list The fallback list
     * @return If not empty, the captured list otherwise the fallback list
     */
    public final ListMultimap<K, V> orElse(ListMultimap<K, V> list) {
        return this.captured == null ? list : this.captured.isEmpty() ? list : this.captured;
    }

    /**
     * If not empty, returns a sequential stream of values associated with key.
     * 
     * @param key The key
     * @return A sequential stream of values
     */
    public final Stream<V> stream(K key) {
        // authors note: Multimap#get(K) returns an empty collection if there is no mapping.
        return this.captured == null ? Stream.empty() : this.captured.get(key).stream();
    }

    /**
     * If not empty and key is present, applies the function to the resulting values.
     * 
     * @param key The key
     * @param function The function to apply
     * @return The function result or null if no key was found
     */
    @Nullable
    public final <U> U mapIfPresent(K key, Function<List<V>, ? extends U> function) {
        if (this.isEmpty()) {
            return null;
        }

        final List<V> values = this.captured.get(key);
        if (values.isEmpty()) {
            return null;
        }

        return function.apply(values);
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
