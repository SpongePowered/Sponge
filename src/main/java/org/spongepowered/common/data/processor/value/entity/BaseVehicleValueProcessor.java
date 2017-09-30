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

public class BaseVehicleValueProcessor extends AbstractSpongeValueProcessor<net.minecraft.entity.Entity, EntitySnapshot, Value<EntitySnapshot>> {

    public BaseVehicleValueProcessor() {
        super(net.minecraft.entity.Entity.class, Keys.BASE_VEHICLE);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected Value<EntitySnapshot> constructValue(EntitySnapshot defaultValue) {
        return new SpongeValue<>(this.getKey(), defaultValue);
    }

    @Override
    protected boolean set(net.minecraft.entity.Entity container, EntitySnapshot value) {
        return ((Entity) container).setVehicle(value.restore().orElse(null));
    }

    @Override
    protected Optional<EntitySnapshot> getVal(net.minecraft.entity.Entity container) {
        return Optional.ofNullable(container.getLowestRidingEntity() == null ? null : ((Entity) container.getLowestRidingEntity()).createSnapshot());
    }

    @Override
    protected ImmutableValue<EntitySnapshot> constructImmutableValue(EntitySnapshot value) {
        return new ImmutableSpongeValue<>(this.getKey(), value);
    }

}
