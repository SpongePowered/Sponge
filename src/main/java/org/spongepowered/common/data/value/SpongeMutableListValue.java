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

public class SpongeMutableListValue<E> extends SpongeCollectionValue.Mutable<E, List<E>, ListValue.Mutable<E>, ListValue.Immutable<E>>
        implements ListValue.Mutable<E> {

    public SpongeMutableListValue(Key<? extends Value<List<E>>> key, List<E> value) {
        super(key, value);
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
    public ListValue.Mutable<E> add(int index, E value) {
        this.value.add(index, value);
        return this;
    }

    @Override
    public ListValue.Mutable<E> add(int index, Iterable<E> values) {
        for (E value : values) {
            add(index++, value);
        }
        return this;
    }

    @Override
    public ListValue.Mutable<E> remove(int index) {
        this.value.remove(index);
        return this;
    }

    @Override
    public ListValue.Mutable<E> set(int index, E element) {
        this.value.set(index, element);
        return this;
    }

    @Override
    public ListValue.Immutable<E> asImmutable() {
        return new SpongeImmutableListValue<>(this.key, CopyHelper.copyList(this.value));
    }

    @Override
    public ListValue.Mutable<E> copy() {
        return new SpongeMutableListValue<>(this.key, CopyHelper.copyList(this.value));
    }
}
