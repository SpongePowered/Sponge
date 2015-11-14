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

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.immutable.ImmutableMobSpawnerData;
import org.spongepowered.api.data.manipulator.mutable.MobSpawnerData;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.util.weighted.WeightedSerializableObject;
import org.spongepowered.common.data.value.mutable.SpongeNextEntityToSpawnValue;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;

import java.util.Collection;
import java.util.function.Function;

import javax.annotation.Nullable;

public class ImmutableSpongeEntityToSpawnValue extends ImmutableSpongeValue<WeightedSerializableObject<EntitySnapshot>> implements
                                                                                            ImmutableMobSpawnerData.ImmutableNextEntityToSpawnValue {

    public ImmutableSpongeEntityToSpawnValue(WeightedSerializableObject<EntitySnapshot> actualValue) {
        super(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN, new WeightedSerializableObject<>(new SpongeEntitySnapshotBuilder().type(EntityTypes.CREEPER).build(), 1), actualValue);
    }

    @Override
    public ImmutableSpongeEntityToSpawnValue with(WeightedSerializableObject<EntitySnapshot> value) {
        return new ImmutableSpongeEntityToSpawnValue(checkNotNull(value));
    }

    @Override
    public ImmutableSpongeEntityToSpawnValue transform(Function<WeightedSerializableObject<EntitySnapshot>, WeightedSerializableObject<EntitySnapshot>> function) {
        final WeightedSerializableObject<EntitySnapshot> value = checkNotNull(function).apply(get());
        return new ImmutableSpongeEntityToSpawnValue(checkNotNull(value));
    }

    @Override
    public MobSpawnerData.NextEntityToSpawnValue asMutable() {
        return new SpongeNextEntityToSpawnValue(this.actualValue);
    }

    @Override
    public ImmutableMobSpawnerData.ImmutableNextEntityToSpawnValue with(EntityType type,
                                                                        @Nullable Collection<DataManipulator<?, ?>> additionalProperties) {
        final EntitySnapshot.Builder builder = new SpongeEntitySnapshotBuilder();
        builder.type(type);
        if (additionalProperties != null) {
            additionalProperties.forEach(builder::add);
        }

        return new ImmutableSpongeEntityToSpawnValue(new WeightedSerializableObject<>(builder.build(), 1));
    }
}
