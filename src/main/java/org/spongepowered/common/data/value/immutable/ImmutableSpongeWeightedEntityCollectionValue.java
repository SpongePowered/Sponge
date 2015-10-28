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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableWeightedEntityCollectionValue;
import org.spongepowered.api.data.value.mutable.WeightedEntityCollectionValue;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntitySnapshotBuilder;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.util.weighted.WeightedCollection;
import org.spongepowered.api.util.weighted.WeightedSerializableObject;
import org.spongepowered.common.data.value.mutable.SpongeWeightedEntityCollectionValue;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;

import java.util.Collection;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class ImmutableSpongeWeightedEntityCollectionValue extends ImmutableSpongeWeightedCollectionValue<WeightedSerializableObject<EntitySnapshot>,
    ImmutableWeightedEntityCollectionValue, WeightedEntityCollectionValue> implements ImmutableWeightedEntityCollectionValue {

    public ImmutableSpongeWeightedEntityCollectionValue(Key<? extends BaseValue<WeightedCollection<WeightedSerializableObject<EntitySnapshot>>>> key) {
        super(key);
    }

    public ImmutableSpongeWeightedEntityCollectionValue(Key<? extends BaseValue<WeightedCollection<WeightedSerializableObject<EntitySnapshot>>>> key,
                                                        WeightedCollection<WeightedSerializableObject<EntitySnapshot>> actualValue) {
        super(key, actualValue);
    }

    @Override
    public ImmutableWeightedEntityCollectionValue with(WeightedCollection<WeightedSerializableObject<EntitySnapshot>> value) {
        final WeightedCollection<WeightedSerializableObject<EntitySnapshot>> weightedEntities = new WeightedCollection<>();
        weightedEntities.addAll(this.actualValue);
        weightedEntities.addAll(checkNotNull(value));
        return new ImmutableSpongeWeightedEntityCollectionValue(getKey(), weightedEntities);
    }

    @Override
    public ImmutableWeightedEntityCollectionValue transform(
        Function<WeightedCollection<WeightedSerializableObject<EntitySnapshot>>, WeightedCollection<WeightedSerializableObject<EntitySnapshot>>> function) {
        final WeightedCollection<WeightedSerializableObject<EntitySnapshot>> weightedEntities = new WeightedCollection<>();
        final WeightedCollection<WeightedSerializableObject<EntitySnapshot>> temp = new WeightedCollection<>();
        temp.addAll(this.actualValue);
        weightedEntities.addAll(checkNotNull(checkNotNull(function).apply(temp)));
        return new ImmutableSpongeWeightedEntityCollectionValue(getKey(), weightedEntities);
    }

    @Override
    public ImmutableWeightedEntityCollectionValue withElement(WeightedSerializableObject<EntitySnapshot> elements) {
        final WeightedCollection<WeightedSerializableObject<EntitySnapshot>> weightedEntities = new WeightedCollection<>();
        weightedEntities.addAll(this.actualValue);
        weightedEntities.add(elements);
        return new ImmutableSpongeWeightedEntityCollectionValue(getKey(), weightedEntities);
    }

    @Override
    public ImmutableWeightedEntityCollectionValue withAll(Iterable<WeightedSerializableObject<EntitySnapshot>> elements) {
        final WeightedCollection<WeightedSerializableObject<EntitySnapshot>> weightedEntities = new WeightedCollection<>();
        weightedEntities.addAll(this.actualValue);
        Iterables.addAll(weightedEntities, elements);
        return new ImmutableSpongeWeightedEntityCollectionValue(getKey(), weightedEntities);
    }

    @Override
    public ImmutableWeightedEntityCollectionValue without(WeightedSerializableObject<EntitySnapshot> element) {
        final WeightedCollection<WeightedSerializableObject<EntitySnapshot>> weightedEntities =
            this.actualValue.stream().filter(entity -> !entity.equals(element)).collect(Collectors.toCollection(WeightedCollection::new));
        return new ImmutableSpongeWeightedEntityCollectionValue(getKey(), weightedEntities);
    }

    @Override
    public ImmutableWeightedEntityCollectionValue withoutAll(Iterable<WeightedSerializableObject<EntitySnapshot>> elements) {
        final WeightedCollection<WeightedSerializableObject<EntitySnapshot>> weightedEntities =
            this.actualValue.stream().filter(entity -> !Iterables.contains(elements, entity))
                .collect(Collectors.toCollection(WeightedCollection::new));
        return new ImmutableSpongeWeightedEntityCollectionValue(getKey(), weightedEntities);
    }

    @Override
    public ImmutableWeightedEntityCollectionValue withoutAll(Predicate<WeightedSerializableObject<EntitySnapshot>> predicate) {
        final WeightedCollection<WeightedSerializableObject<EntitySnapshot>> weightedEntities =
            this.actualValue.stream().filter(entity -> !predicate.test(entity)).collect(Collectors.toCollection(WeightedCollection::new));
        return new ImmutableSpongeWeightedEntityCollectionValue(getKey(), weightedEntities);
    }

    @Override
    public WeightedEntityCollectionValue asMutable() {
        return new SpongeWeightedEntityCollectionValue(getKey(), getAll());
    }

    @Override
    public ImmutableWeightedEntityCollectionValue with(EntityType entityType, Collection<DataManipulator<?, ?>> entityData) {
        EntitySnapshotBuilder builder = new SpongeEntitySnapshotBuilder();
        builder.type(entityType);
        entityData.forEach(builder::add);
        return withElement(new WeightedSerializableObject<>(builder.build(), 1));
    }

    @Nullable
    @Override
    public WeightedSerializableObject<EntitySnapshot> get(Random random) {
        return this.actualValue.get(checkNotNull(random));
    }
}
