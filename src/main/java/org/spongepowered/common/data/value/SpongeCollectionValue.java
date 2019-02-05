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

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.CollectionValue;
import org.spongepowered.api.data.value.Value;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class SpongeCollectionValue<E, C extends Collection<E>> extends SpongeValue<C> implements CollectionValue<E, C> {

    protected SpongeCollectionValue(Key<? extends Value<C>> key, C value) {
        super(key, value);
    }

    @Override
    public int size() {
        return this.value.size();
    }

    @Override
    public boolean isEmpty() {
        return this.value.isEmpty();
    }

    @Override
    public boolean contains(E element) {
        return this.value.contains(element);
    }

    @Override
    public boolean containsAll(Iterable<E> iterable) {
        if (iterable instanceof Collection) {
            return this.value.containsAll((Collection) iterable);
        }
        for (E element : iterable) {
            if (!this.value.contains(element)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public C getAll() {
        return CopyHelper.copy(this.value);
    }

    @Override
    public Iterator<E> iterator() {
        return get().iterator();
    }

    @SuppressWarnings("unchecked")
    public static abstract class Mutable<E, C extends Collection<E>, M extends CollectionValue.Mutable<E, C, M, I>, I extends CollectionValue.Immutable<E, C, I, M>>
            extends SpongeCollectionValue<E, C> implements CollectionValue.Mutable<E, C, M, I> {

        protected Mutable(Key<? extends Value<C>> key, C value) {
            super(key, value);
        }

        @Override
        public M add(E element) {
            this.value.add(element);
            return (M) this;
        }

        @Override
        public M addAll(Iterable<E> elements) {
            elements.forEach(this::add);
            return (M) this;
        }

        @Override
        public M remove(E element) {
            this.value.remove(element);
            return (M) this;
        }

        @Override
        public M removeAll(Iterable<E> elements) {
            elements.forEach(this::remove);
            return (M) this;
        }

        @Override
        public M removeAll(Predicate<E> predicate) {
            this.value.removeIf(predicate);
            return (M) this;
        }

        @Override
        public M set(C value) {
            this.value = checkNotNull(value, "value");
            return (M) this;
        }

        @Override
        public M transform(Function<C, C> function) {
            return set(checkNotNull(function, "function").apply(get()));
        }
    }

    @SuppressWarnings("unchecked")
    public static abstract class Immutable<E, C extends Collection<E>, I extends CollectionValue.Immutable<E, C, I, M>, M extends CollectionValue.Mutable<E, C, M, I>>
            extends SpongeCollectionValue<E, C> implements CollectionValue.Immutable<E, C, I, M> {

        protected Immutable(Key<? extends Value<C>> key, C value) {
            super(key, value);
        }

        @Override
        public C get() {
            return CopyHelper.copy(super.get());
        }

        @Override
        public I withElement(E element) {
            final C collection = get();
            if (collection.add(element)) {
                return withValue(collection);
            }
            return (I) this;
        }

        @Override
        public I withAll(Iterable<E> elements) {
            boolean change = false;
            final C collection = get();
            for (E element : elements) {
                change = collection.add(element) || change;
            }
            return change ? withValue(collection) : (I) this;
        }

        @Override
        public I without(E element) {
            if (!contains(element)) {
                return (I) this;
            }
            final C collection = get();
            collection.remove(element);
            return withValue(collection);
        }

        @Override
        public I withoutAll(Iterable<E> elements) {
            final C collection = get();
            elements.forEach(collection::remove);
            return withValue(collection);
        }

        @Override
        public I withoutAll(Predicate<E> predicate) {
            final C collection = get();
            collection.removeIf(predicate);
            return withValue(collection);
        }

        @Override
        public I with(C value) {
            return withValue(CopyHelper.copy(value));
        }

        /**
         * Constructs a new {@link SpongeCollectionValue.Immutable}
         * without copying the actual value.
         *
         * @param value The value element
         * @return The new immutable value
         */
        protected abstract I withValue(C value);

        @Override
        public I transform(Function<C, C> function) {
            return with(checkNotNull(function, "function").apply(get()));
        }
    }
}
