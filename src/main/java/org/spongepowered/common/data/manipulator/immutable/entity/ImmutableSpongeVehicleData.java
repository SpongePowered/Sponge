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
package org.spongepowered.common.data.manipulator.immutable.entity;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableVehicleData;
import org.spongepowered.api.data.manipulator.mutable.entity.VehicleData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeVehicleData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.immutable.common.ImmutableSpongeEntityValue;

import java.lang.ref.WeakReference;

public class ImmutableSpongeVehicleData extends AbstractImmutableData<ImmutableVehicleData, VehicleData> implements ImmutableVehicleData {

    private final WeakReference<Entity> vehicle;
    private final WeakReference<Entity> baseVehicle;

    public ImmutableSpongeVehicleData(Entity vehicle, Entity baseVehicle) {
        super(ImmutableVehicleData.class);
        this.vehicle = new WeakReference<>(vehicle);
        this.baseVehicle = new WeakReference<>(baseVehicle);
        registerGetters();
    }

    @Override
    public ImmutableValue<Entity> vehicle() {
        checkState(this.vehicle.get() != null);
        return new ImmutableSpongeEntityValue(Keys.VEHICLE, this.vehicle.get());
    }

    @Override
    public ImmutableValue<Entity> baseVehicle() {
        checkState(this.baseVehicle.get() != null);
        return new ImmutableSpongeEntityValue(Keys.BASE_VEHICLE, this.baseVehicle.get());
    }

    @Override
    public VehicleData asMutable() {
        checkState(this.vehicle.get() != null);
        checkState(this.baseVehicle.get() != null);
        return new SpongeVehicleData(this.vehicle.get(), this.baseVehicle.get());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
            .set(Keys.VEHICLE.getQuery(), this.vehicle.get().getUniqueId())
            .set(Keys.BASE_VEHICLE.getQuery(), this.baseVehicle.get().getUniqueId());
    }

    @Override
    public int compareTo(ImmutableVehicleData o) {
        return ComparisonChain.start()
                .compare(o.vehicle().get().getUniqueId(), this.vehicle.get().getUniqueId())
                .compare(o.baseVehicle().get().getUniqueId(), this.baseVehicle.get().getUniqueId())
                .result();
    }

    public Entity getVehicle() {
        checkState(this.vehicle.get() != null);
        return checkNotNull(this.vehicle.get());
    }

    public Entity getBaseVehicle() {
        checkState(this.baseVehicle.get() != null);
        return checkNotNull(this.baseVehicle.get());
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.VEHICLE, ImmutableSpongeVehicleData.this::getVehicle);
        registerKeyValue(Keys.VEHICLE, ImmutableSpongeVehicleData.this::vehicle);

        registerFieldGetter(Keys.BASE_VEHICLE, ImmutableSpongeVehicleData.this::getBaseVehicle);
        registerKeyValue(Keys.BASE_VEHICLE, ImmutableSpongeVehicleData.this::baseVehicle);
    }

}
