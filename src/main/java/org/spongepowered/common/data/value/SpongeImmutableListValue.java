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

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.ListValue;
import org.spongepowered.api.data.value.Value;

import java.util.List;

public class SpongeImmutableListValue<E> extends SpongeCollectionValue.Immutable<E, List<E>, ListValue.Immutable<E>, ListValue.Mutable<E>>
        implements ListValue.Immutable<E> {

    public SpongeImmutableListValue(Key<? extends Value<List<E>>> key, List<E> value) {
        super(key, value);
    }

    @Override
    protected ListValue.Immutable<E> withValue(List<E> value) {
        return new SpongeImmutableListValue<>(this.key, value);
    }

    @Override
    public List<E> get() {
        return CopyHelper.copyList(super.get());
    }

    @Override
    public E get(int index) {
        return this.value.get(index);
    }

    @Override
    public int indexOf(E element) {
        return this.value.indexOf(element);
    }

    @Override
    public ListValue.Immutable<E> with(int index, E value) {
        final List<E> list = get();
        list.add(index, value);
        return withValue(list);
    }

    @Override
    public ListValue.Immutable<E> with(int index, Iterable<E> values) {
        final List<E> list = get();
        for (E value : values) {
            list.add(index++, value);
        }
        return withValue(list);
    }

    @Override
    public ListValue.Immutable<E> without(int index) {
        final List<E> list = get();
        list.remove(index);
        return withValue(list);
    }

    @Override
    public ListValue.Immutable<E> set(int index, E element) {
        final List<E> list = get();
        list.set(index, element);
        return withValue(list);
    }

    @Override
    public ListValue.Mutable<E> asMutable() {
        return new SpongeMutableListValue<>(this.key, get());
    }
}
