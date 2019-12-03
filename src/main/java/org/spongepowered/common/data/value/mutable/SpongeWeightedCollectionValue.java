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

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.WeightedCollectionValue.Mutable;
import org.spongepowered.api.data.value.WeightedcollectionValue.Immutable;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.util.weighted.TableEntry;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeWeightedCollectionValue;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class SpongeWeightedCollectionValue<E> extends SpongeCollectionValue<TableEntry<E>,
    WeightedTable<E>, Mutable<E>, Immutable<E>> implements Mutable<E> {

    public SpongeWeightedCollectionValue(Key<? extends Value<WeightedTable<E>>> key, WeightedTable<E> actualValue) {
        super(key, new WeightedTable<>(), actualValue.stream().collect(Collectors.toCollection(WeightedTable<E>::new)));
    }

    @Override
    public Mutable<E> filter(Predicate<? super TableEntry<E>> predicate) {
        return this.set(this.get().stream().filter(predicate).collect(Collectors.toCollection(WeightedTable<E>::new)));
    }

    @Override
    public WeightedTable<E> getAll() {
        return this.get().stream().collect(Collectors.toCollection(WeightedTable<E>::new));
    }

    @Override
    public Immutable<E> asImmutable() {
        return new ImmutableSpongeWeightedCollectionValue<>(this.getKey(), this.getAll());
    }

    @Override
    public Mutable<E> copy() {
        return new SpongeWeightedCollectionValue<>(this.getKey(), this.getAll());
    }

    @Nullable
    @Override
    public List<E> get(Random random) {
        return this.get().get(random);
    }
}
