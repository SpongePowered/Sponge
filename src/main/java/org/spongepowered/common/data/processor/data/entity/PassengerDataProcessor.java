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
package org.spongepowered.common.data.processor.data.entity;

import static org.spongepowered.common.data.util.DataUtil.getData;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePassengerData;
import org.spongepowered.api.data.manipulator.mutable.entity.BreathingData;
import org.spongepowered.api.data.manipulator.mutable.entity.PassengerData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongePassengerData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.SpongeEntitySnapshot;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;

import java.util.Optional;

public class PassengerDataProcessor extends AbstractEntitySingleDataProcessor<net.minecraft.entity.Entity, EntitySnapshot, Value<EntitySnapshot>, PassengerData, ImmutablePassengerData> {

    public PassengerDataProcessor() {
        super(net.minecraft.entity.Entity.class, Keys.PASSENGER);
    }

    @Override
    protected boolean set(net.minecraft.entity.Entity entity, EntitySnapshot snapshot) {
        Optional<Entity> passenger = snapshot.restore();
        return EntityUtil.setPassenger(entity, (net.minecraft.entity.Entity) passenger.orElse(null));
    }

    @Override
    protected Optional<EntitySnapshot> getVal(net.minecraft.entity.Entity dataHolder) {
        if (dataHolder.riddenByEntity != null) {
            return Optional.of(((Entity) dataHolder.riddenByEntity).createSnapshot());
        }
        return Optional.empty();
    }

    @Override
    protected ImmutableValue<EntitySnapshot> constructImmutableValue(EntitySnapshot value) {
        return new ImmutableSpongeValue<>(Keys.PASSENGER, value);
    }

    @Override
    protected Value<EntitySnapshot> constructValue(EntitySnapshot actualValue) {
        return new SpongeValue<EntitySnapshot>(Keys.PASSENGER, actualValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (this.supports(container)) {
            net.minecraft.entity.Entity entity = ((net.minecraft.entity.Entity) container);
            if (entity.riddenByEntity != null) {
                net.minecraft.entity.Entity passenger = entity.riddenByEntity;
                EntityUtil.setPassenger(entity, null);
                return DataTransactionResult.builder().result(DataTransactionResult.Type.SUCCESS).replace(constructImmutableValue(((Entity) passenger).createSnapshot())).build();
            }
            return DataTransactionResult.successNoData();
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public Optional<PassengerData> fill(DataContainer container, PassengerData passengerData) {
        passengerData.set(Keys.PASSENGER, container.getSerializable(Keys.PASSENGER.getQuery(), EntitySnapshot.class).get());
        return Optional.of(passengerData);
    }

    @Override
    protected PassengerData createManipulator() {
        return new SpongePassengerData();
    }
}
