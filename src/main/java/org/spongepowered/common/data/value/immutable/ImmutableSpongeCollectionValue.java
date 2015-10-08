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
package org.spongepowered.common.data.value.immutable;

import com.google.common.collect.Iterables;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableCollectionValue;
import org.spongepowered.api.data.value.mutable.CollectionValue;

import java.util.Collection;
import java.util.function.Function;

public abstract class ImmutableSpongeCollectionValue<E, V extends Collection<E>, I extends ImmutableCollectionValue<E, V, I, L>,
    L extends CollectionValue<E, V, L, I>> extends ImmutableSpongeValue<V> implements ImmutableCollectionValue<E, V, I, L> {

    protected ImmutableSpongeCollectionValue(Key<? extends BaseValue<V>> key, V defaultValue) {
        super(key, defaultValue);
    }

    protected ImmutableSpongeCollectionValue(Key<? extends BaseValue<V>> key, V defaultValue, V actualValue) {
        super(key, defaultValue, actualValue);
    }

    @Override
    public abstract I with(V value);

    @Override
    public abstract I transform(Function<V, V> function);

    @Override
    public abstract L asMutable();

    @Override
    public int size() {
        return this.actualValue.size();
    }

    @Override
    public boolean isEmpty() {
        return this.actualValue.isEmpty();
    }


    @Override
    public boolean contains(E element) {
        return this.actualValue.contains(element);
    }

    @Override
    public boolean containsAll(Iterable<E> iterable) {
        for (E element : iterable) {
            if (!Iterables.contains(this.actualValue, element)) {
                return false;
            }
        }
        return true;
    }
}
