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
package org.spongepowered.common.data.value.mutable;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableCollectionValue;
import org.spongepowered.api.data.value.mutable.CollectionValue;

import java.util.Collection;
import java.util.Iterator;

@SuppressWarnings("unchecked")
public abstract class SpongeCollectionValue<E, V extends Collection<E>, I extends CollectionValue<E, V, I, L>,
    L extends ImmutableCollectionValue<E, V, L, I>> extends SpongeValue<V> implements CollectionValue<E, V, I, L> {


    public SpongeCollectionValue(Key<? extends BaseValue<V>> key, V defaultValue) {
        super(key, defaultValue);
    }

    public SpongeCollectionValue(Key<? extends BaseValue<V>> key, V defaultValue, V actualValue) {
        super(key, defaultValue, actualValue);
    }

    @Override
    public I set(V value) {
        this.actualValue = checkNotNull(value);
        return (I) this;
    }

    @Override
    public I transform(Function<V, V> function) {
        this.actualValue = checkNotNull(function).apply(this.actualValue);
        return (I) this;
    }

    @Override
    public int size() {
        return this.actualValue.size();
    }

    @Override
    public boolean isEmpty() {
        return this.actualValue.isEmpty();
    }

    @Override
    public I add(E element) {
        this.actualValue.add(checkNotNull(element));
        return (I) this;
    }

    @Override
    public I addAll(Iterable<E> elements) {
        for (E element : checkNotNull(elements)) {
            this.actualValue.add(checkNotNull(element));
        }
        return (I) this;
    }

    @Override
    public I remove(E element) {
        this.actualValue.remove(checkNotNull(element));
        return (I) this;
    }

    @Override
    public I removeAll(Iterable<E> elements) {
        for (E element : elements) {
            this.actualValue.remove(checkNotNull(element));
        }
        return (I) this;
    }

    @Override
    public I removeAll(Predicate<E> predicate) {
        for (Iterator<E> iterator = this.actualValue.iterator(); iterator.hasNext(); ) {
            if (checkNotNull(predicate).apply(iterator.next())) {
                iterator.remove();
            }
        }
        return (I) this;
    }

    @Override
    public boolean contains(E element) {
        return this.actualValue.contains(checkNotNull(element));
    }

    @Override
    public boolean containsAll(Collection<E> iterable) {
        return this.actualValue.containsAll(iterable);
    }

    @Override
    public boolean exists() {
        return this.actualValue != null;
    }

    @Override
    public abstract L asImmutable();

    @Override
    public Optional<V> getDirect() {
        return Optional.of(this.actualValue);
    }

    @Override
    public Iterator<E> iterator() {
        return this.actualValue.iterator();
    }
}
