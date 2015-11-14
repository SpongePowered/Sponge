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

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableWeightedEntityCollectionValue;
import org.spongepowered.api.data.value.mutable.WeightedEntityCollectionValue;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.util.weighted.WeightedCollection;
import org.spongepowered.api.util.weighted.WeightedSerializableObject;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeWeightedEntityCollectionValue;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SpongeWeightedEntityCollectionValue extends SpongeWeightedCollectionValue<WeightedSerializableObject<EntitySnapshot>, WeightedEntityCollectionValue,
    ImmutableWeightedEntityCollectionValue> implements WeightedEntityCollectionValue {

    public SpongeWeightedEntityCollectionValue(Key<? extends BaseValue<WeightedCollection<WeightedSerializableObject<EntitySnapshot>>>> key) {
        super(key);
    }

    public SpongeWeightedEntityCollectionValue(Key<? extends BaseValue<WeightedCollection<WeightedSerializableObject<EntitySnapshot>>>> key,
                                               WeightedCollection<WeightedSerializableObject<EntitySnapshot>> actualValue) {
        super(key, actualValue);
    }

    @Override
    public WeightedEntityCollectionValue filter(Predicate<? super WeightedSerializableObject<EntitySnapshot>> predicate) {
        final WeightedCollection<WeightedSerializableObject<EntitySnapshot>> collection =
            this.actualValue.stream().filter(entity -> checkNotNull(predicate).test(entity))
                .collect(Collectors.toCollection(WeightedCollection::new));
        return new SpongeWeightedEntityCollectionValue(getKey(), collection);
    }

    @Override
    public WeightedCollection<WeightedSerializableObject<EntitySnapshot>> getAll() {
        return this.actualValue.stream().collect(Collectors.toCollection(WeightedCollection::new));
    }

    @Override
    public ImmutableWeightedEntityCollectionValue asImmutable() {
        return new ImmutableSpongeWeightedEntityCollectionValue(getKey(), this.actualValue);
    }

    @Override
    public WeightedEntityCollectionValue add(EntityType entityType, Collection<DataManipulator<?, ?>> entityData) {
        final EntitySnapshot.Builder builder = new SpongeEntitySnapshotBuilder();
        builder.type(entityType);
        entityData.forEach(builder::add);
        return add(new WeightedSerializableObject<>(builder.build(), 1));
    }
}
