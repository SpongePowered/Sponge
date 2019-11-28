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
package org.spongepowered.common.data.processor.value.entity;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class VehicleValueProcessor extends AbstractSpongeValueProcessor<net.minecraft.entity.Entity, EntitySnapshot, Value<EntitySnapshot>> {

    public VehicleValueProcessor() {
        super(net.minecraft.entity.Entity.class, Keys.VEHICLE);
    }

    @Override
    public boolean supports(final ValueContainer<?> container) {
        return container instanceof net.minecraft.entity.Entity;
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        if (container instanceof net.minecraft.entity.Entity) {
            final net.minecraft.entity.Entity entity = ((net.minecraft.entity.Entity) container);
            if (entity.isPassenger()) {
                final Entity vehicle = (Entity) entity.getRidingEntity();
                entity.stopRiding();
                return DataTransactionResult.successResult(new ImmutableSpongeValue<>(Keys.VEHICLE, vehicle.createSnapshot()));
            }
            return DataTransactionResult.builder().result(DataTransactionResult.Type.SUCCESS).build();
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    protected Value<EntitySnapshot> constructValue(final EntitySnapshot defaultValue) {
        return new SpongeValue<>(this.getKey(), defaultValue);
    }

    @Override
    protected boolean set(final net.minecraft.entity.Entity container, final EntitySnapshot value) {
        return ((Entity) container).setVehicle(value.restore().orElse(null));
    }

    @Override
    protected Optional<EntitySnapshot> getVal(final net.minecraft.entity.Entity container) {
        final Entity entity = (Entity) container.getRidingEntity();
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(entity.createSnapshot());
    }

    @Override
    protected ImmutableValue<EntitySnapshot> constructImmutableValue(final EntitySnapshot value) {
        return new ImmutableSpongeValue<>(this.getKey(), value);
    }

}
