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
package org.spongepowered.common.data.value;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.CollectionValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public abstract class ImmutableSpongeCollectionValue<E, C extends Collection<E>,
        I extends CollectionValue.Immutable<E, C, I, M>, M extends CollectionValue.Mutable<E, C, M, I>>
        extends AbstractImmutableSpongeValue<C> implements CollectionValue.Immutable<E, C, I, M> {

    public ImmutableSpongeCollectionValue(Key<? extends CollectionValue<E, C>> key, C element) {
        super(key, element);
    }

    @Override
    public int size() {
        return this.element.size();
    }

    @Override
    public boolean isEmpty() {
        return this.element.isEmpty();
    }

    @Override
    public boolean contains(E element) {
        return this.element.contains(element);
    }

    @Override
    public boolean containsAll(Iterable<E> iterable) {
        if (iterable instanceof Collection) {
            return this.element.containsAll((Collection<?>) iterable);
        }
        return Streams.stream(iterable).allMatch(this::contains);
    }

    @Override
    public C getAll() {
        return this.get();
    }

    protected abstract I modifyCollection(Consumer<C> consumer);

    @Override
    public I withElement(E element) {
        return this.modifyCollection(collection -> collection.add(element));
    }

    @Override
    public I withAll(Iterable<E> elements) {
        return this.modifyCollection(collection -> Iterables.addAll(collection, elements));
    }

    @Override
    public I without(E element) {
        if (!this.contains(element)) {
            return (I) this;
        }
        return this.modifyCollection(collection -> collection.remove(element));
    }

    @Override
    public I withoutAll(Iterable<E> elements) {
        if (Streams.stream(elements).noneMatch(this::contains)) {
            return (I) this;
        }
        return this.modifyCollection(collection -> elements.forEach(collection::remove));
    }

    @Override
    public I withoutAll(Predicate<E> predicate) {
        return this.modifyCollection(collection -> collection.removeIf(predicate));
    }

    @Override
    public I transform(Function<C, C> function) {
        return this.with(function.apply(this.get()));
    }

    @Override
    public Iterator<E> iterator() {
        return this.get().iterator();
    }
}
