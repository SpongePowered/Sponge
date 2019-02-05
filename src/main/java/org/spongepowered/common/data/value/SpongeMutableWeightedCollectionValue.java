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
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.WeightedCollectionValue;
import org.spongepowered.api.util.weighted.TableEntry;
import org.spongepowered.api.util.weighted.WeightedTable;

import java.util.List;
import java.util.Random;

public class SpongeMutableWeightedCollectionValue<E> extends SpongeCollectionValue.Mutable<TableEntry<E>, WeightedTable<E>,
        WeightedCollectionValue.Mutable<E>, WeightedCollectionValue.Immutable<E>> implements WeightedCollectionValue.Mutable<E> {

    public SpongeMutableWeightedCollectionValue(Key<? extends Value<WeightedTable<E>>> key, WeightedTable<E> value) {
        super(key, value);
    }

    @Override
    public WeightedCollectionValue.Mutable<E> copy() {
        return new SpongeMutableWeightedCollectionValue<>(this.key, CopyHelper.copyWeightedTable(this.value));
    }

    @Override
    public List<E> get(Random random) {
        return this.value.get(random);
    }

    @Override
    public WeightedCollectionValue.Immutable<E> asImmutable() {
        return new SpongeImmutableWeightedCollectionValue<>(this.key, CopyHelper.copyWeightedTable(this.value));
    }
}
