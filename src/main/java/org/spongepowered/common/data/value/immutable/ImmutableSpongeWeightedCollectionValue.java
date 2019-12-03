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
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.WeightedCollectionValue.Mutable;
import org.spongepowered.api.data.value.WeightedcollectionValue.Immutable;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.util.weighted.TableEntry;
import org.spongepowered.common.data.value.mutable.SpongeWeightedCollectionValue;

import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class ImmutableSpongeWeightedCollectionValue<E> extends ImmutableSpongeCollectionValue<TableEntry<E>,
    WeightedTable<E>, Immutable<E>, Mutable<E>> implements Immutable<E> {


    public ImmutableSpongeWeightedCollectionValue(Key<? extends Value<WeightedTable<E>>> key, WeightedTable<E> actualValue) {
        super(key, new WeightedTable<>(), actualValue.stream().collect(Collectors.toCollection(WeightedTable<E>::new)));
    }

    @Override
    public Immutable<E> with(WeightedTable<E> value) {
        return new ImmutableSpongeWeightedCollectionValue<>(this.getKey(), value);
    }

    @Override
    public Immutable<E> withElement(TableEntry<E> elements) {
        WeightedTable<E> table = new WeightedTable<>();
        table.addAll(this.actualValue);
        table.add(elements);
        return new ImmutableSpongeWeightedCollectionValue<>(this.getKey(), table);
    }

    @Override
    public Immutable<E> transform(Function<WeightedTable<E>, WeightedTable<E>> function) {
        final WeightedTable<E> table = this.getAll();
        final WeightedTable<E> functionTable = function.apply(table);
        return new ImmutableSpongeWeightedCollectionValue<>(this.getKey(), functionTable);
    }

    @Override
    public Immutable<E> withAll(Iterable<TableEntry<E>> elements) {
        final WeightedTable<E> newTable = this.getAll();
        elements.forEach(newTable::add);
        return new ImmutableSpongeWeightedCollectionValue<>(this.getKey(), newTable);
    }

    @Override
    public Immutable<E> without(TableEntry<E> element) {
        final WeightedTable<E> newTable = this.actualValue.stream()
                .filter(entry -> !entry.equals(element))
                .map(entry -> element)
                .collect(Collectors.toCollection(WeightedTable::new));
        return new ImmutableSpongeWeightedCollectionValue<>(this.getKey(), newTable);
    }

    @Override
    public Immutable<E> withoutAll(Iterable<TableEntry<E>> elements) {
        final WeightedTable<E> newTable = new WeightedTable<>();
        this.actualValue.stream()
            .filter(entry -> !Iterables.contains(elements, entry))
            .forEach(newTable::add);
        return new ImmutableSpongeWeightedCollectionValue<>(this.getKey(), newTable);
    }

    @Override
    public Immutable<E> withoutAll(Predicate<TableEntry<E>> predicate) {
        final WeightedTable<E> newTable = this.actualValue.stream()
            .filter(predicate)
            .collect(Collectors.toCollection(WeightedTable::new));
        return new ImmutableSpongeWeightedCollectionValue<>(this.getKey(), newTable);
    }

    @Override
    public WeightedTable<E> getAll() {
        final WeightedTable<E> newTable = new WeightedTable<>();
        newTable.addAll(this.actualValue);
        return newTable;
    }

    @Override
    public Mutable<E> asMutable() {
        return new SpongeWeightedCollectionValue<>(this.getKey(), this.getAll());
    }

    @Nullable
    @Override
    public List<E> get(Random random) {
        return this.actualValue.get(random);
    }
}
