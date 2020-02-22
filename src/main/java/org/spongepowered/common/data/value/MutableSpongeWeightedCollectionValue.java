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

import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.WeightedCollectionValue;
import org.spongepowered.api.util.weighted.TableEntry;
import org.spongepowered.api.util.weighted.UnmodifiableWeightedTable;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.common.data.copy.CopyHelper;
import org.spongepowered.common.data.key.SpongeKey;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public final class MutableSpongeWeightedCollectionValue<E> extends MutableSpongeCollectionValue<TableEntry<E>, WeightedTable<E>,
        WeightedCollectionValue.Mutable<E>, WeightedCollectionValue.Immutable<E>> implements WeightedCollectionValue.Mutable<E> {

    public MutableSpongeWeightedCollectionValue(Key<? extends WeightedCollectionValue<E>> key, WeightedTable<E> element) {
        super(key, element);
    }

    @Override
    public SpongeKey<? extends WeightedCollectionValue<E>, WeightedTable<E>> getKey() {
        //noinspection unchecked
        return (SpongeKey<? extends WeightedCollectionValue<E>, WeightedTable<E>>) super.getKey();
    }

    @Override
    public List<E> get(Random random) {
        return this.element.get(random);
    }

    @Override
    public WeightedCollectionValue.Immutable<E> asImmutable() {
        return this.getKey().getValueConstructor().getImmutable(this.element).asImmutable();
    }

    @Override
    public WeightedCollectionValue.Mutable<E> copy() {
        return new MutableSpongeWeightedCollectionValue<>(this.getKey(), CopyHelper.copy(this.element));
    }

    @Override
    protected WeightedCollectionValue.Mutable<E> modifyCollection(Consumer<WeightedTable<E>> consumer) {
        final WeightedTable<E> table = this.element;
        if (table instanceof UnmodifiableWeightedTable) {
            final WeightedTable<E> temp = new WeightedTable<>(table.getRolls());
            temp.addAll(table);
            consumer.accept(temp);
            this.set(new UnmodifiableWeightedTable<>(temp));
        } else {
            consumer.accept(this.element);
        }
        return this;
    }
}
