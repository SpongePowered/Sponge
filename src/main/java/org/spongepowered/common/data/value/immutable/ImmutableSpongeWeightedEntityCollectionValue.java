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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableWeightedEntityCollectionValue;
import org.spongepowered.api.data.value.mutable.WeightedEntityCollectionValue;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.util.weighted.WeightedCollection;
import org.spongepowered.api.util.weighted.WeightedEntity;
import org.spongepowered.common.data.value.mutable.SpongeWeightedEntityCollectionValue;

import java.util.Collection;
import java.util.Random;

import javax.annotation.Nullable;

public class ImmutableSpongeWeightedEntityCollectionValue extends ImmutableSpongeWeightedCollectionValue<WeightedEntity,
    ImmutableWeightedEntityCollectionValue, WeightedEntityCollectionValue> implements ImmutableWeightedEntityCollectionValue {

    public ImmutableSpongeWeightedEntityCollectionValue(Key<? extends BaseValue<WeightedCollection<WeightedEntity>>> key) {
        super(key);
    }

    public ImmutableSpongeWeightedEntityCollectionValue(Key<? extends BaseValue<WeightedCollection<WeightedEntity>>> key,
                                                        WeightedCollection<WeightedEntity> actualValue) {
        super(key, actualValue);
    }

    @Override
    public ImmutableWeightedEntityCollectionValue with(WeightedCollection<WeightedEntity> value) {
        final WeightedCollection<WeightedEntity> weightedEntities = new WeightedCollection<WeightedEntity>();
        weightedEntities.addAll(this.actualValue);
        weightedEntities.addAll(checkNotNull(value));
        return new ImmutableSpongeWeightedEntityCollectionValue(getKey(), weightedEntities);
    }

    @Override
    public ImmutableWeightedEntityCollectionValue transform(
        Function<WeightedCollection<WeightedEntity>, WeightedCollection<WeightedEntity>> function) {
        final WeightedCollection<WeightedEntity> weightedEntities = new WeightedCollection<WeightedEntity>();
        final WeightedCollection<WeightedEntity> temp = new WeightedCollection<WeightedEntity>();
        temp.addAll(this.actualValue);
        weightedEntities.addAll(checkNotNull(checkNotNull(function).apply(temp)));
        return new ImmutableSpongeWeightedEntityCollectionValue(getKey(), weightedEntities);
    }

    @Override
    public ImmutableWeightedEntityCollectionValue with(WeightedEntity... elements) {
        final WeightedCollection<WeightedEntity> weightedEntities = new WeightedCollection<WeightedEntity>();
        weightedEntities.addAll(this.actualValue);
        weightedEntities.addAll(ImmutableList.copyOf(elements));
        return new ImmutableSpongeWeightedEntityCollectionValue(getKey(), weightedEntities);
    }

    @Override
    public ImmutableWeightedEntityCollectionValue withAll(Iterable<WeightedEntity> elements) {
        final WeightedCollection<WeightedEntity> weightedEntities = new WeightedCollection<WeightedEntity>();
        weightedEntities.addAll(this.actualValue);
        Iterables.addAll(weightedEntities, elements);
        return new ImmutableSpongeWeightedEntityCollectionValue(getKey(), weightedEntities);
    }

    @Override
    public ImmutableWeightedEntityCollectionValue without(WeightedEntity element) {
        final WeightedCollection<WeightedEntity> weightedEntities = new WeightedCollection<WeightedEntity>();
        for (WeightedEntity entity : this.actualValue) {
            if (!entity.equals(element)) {
                weightedEntities.add(entity);
            }
        }
        return new ImmutableSpongeWeightedEntityCollectionValue(getKey(), weightedEntities);
    }

    @Override
    public ImmutableWeightedEntityCollectionValue withoutAll(Iterable<WeightedEntity> elements) {
        final WeightedCollection<WeightedEntity> weightedEntities = new WeightedCollection<WeightedEntity>();
        for (WeightedEntity entity : this.actualValue) {
            if (!Iterables.contains(elements, entity)) {
                weightedEntities.add(entity);
            }
        }
        return new ImmutableSpongeWeightedEntityCollectionValue(getKey(), weightedEntities);
    }

    @Override
    public ImmutableWeightedEntityCollectionValue withoutAll(Predicate<WeightedEntity> predicate) {
        final WeightedCollection<WeightedEntity> weightedEntities = new WeightedCollection<WeightedEntity>();
        for (WeightedEntity entity : this.actualValue) {
            if (!predicate.apply(entity)) {
                weightedEntities.add(entity);
            }
        }
        return new ImmutableSpongeWeightedEntityCollectionValue(getKey(), weightedEntities);
    }

    @Override
    public WeightedEntityCollectionValue asMutable() {
        return new SpongeWeightedEntityCollectionValue(getKey(), getAll());
    }

    @Override
    public ImmutableWeightedEntityCollectionValue with(EntityType entityType, Collection<DataManipulator<?, ?>> entityData) {
        return with(new WeightedEntity(entityType, 1, entityData));
    }

    @Nullable
    @Override
    public WeightedEntity get(Random random) {
        return this.actualValue.get(checkNotNull(random));
    }
}
