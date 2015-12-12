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
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeVehicleData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.immutable.common.ImmutableSpongeEntityValue;

import java.lang.ref.WeakReference;

public class ImmutableSpongeVehicleData extends AbstractImmutableData<ImmutableVehicleData, VehicleData> implements ImmutableVehicleData {

    private final EntitySnapshot vehicle;
    private final EntitySnapshot baseVehicle;

    public ImmutableSpongeVehicleData(EntitySnapshot vehicle, EntitySnapshot baseVehicle) {
        super(ImmutableVehicleData.class);
        this.vehicle = vehicle;
        this.baseVehicle = baseVehicle;
        registerGetters();
    }

    public ImmutableSpongeVehicleData(Entity vehicle, Entity baseVehicle) {
        this(vehicle.createSnapshot(), baseVehicle.createSnapshot());
    }

    @Override
    public ImmutableValue<EntitySnapshot> vehicle() {
        checkState(this.vehicle != null);
        return new ImmutableSpongeValue<>(Keys.VEHICLE, this.vehicle);
    }

    @Override
    public ImmutableValue<EntitySnapshot> baseVehicle() {
        checkState(this.baseVehicle != null);
        return new ImmutableSpongeValue<>(Keys.BASE_VEHICLE, this.baseVehicle);
    }

    @Override
    public VehicleData asMutable() {
        checkState(this.vehicle != null);
        checkState(this.baseVehicle != null);
        return new SpongeVehicleData(this.vehicle, this.baseVehicle);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.VEHICLE, this.vehicle)
            .set(Keys.BASE_VEHICLE, this.baseVehicle);
    }

    @Override
    public int compareTo(ImmutableVehicleData o) {
        return ComparisonChain.start()
                .compare(o.vehicle().get().getUniqueId().orElse(null), this.vehicle.getUniqueId().orElse(null))
                .compare(o.baseVehicle().get().getUniqueId().orElse(null), this.baseVehicle.getUniqueId().orElse(null))
                .result();
    }

    public EntitySnapshot getVehicle() {
        checkState(this.vehicle != null);
        return checkNotNull(this.vehicle);
    }

    public EntitySnapshot getBaseVehicle() {
        checkState(this.baseVehicle != null);
        return checkNotNull(this.baseVehicle);
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.VEHICLE, ImmutableSpongeVehicleData.this::getVehicle);
        registerKeyValue(Keys.VEHICLE, ImmutableSpongeVehicleData.this::vehicle);

        registerFieldGetter(Keys.BASE_VEHICLE, ImmutableSpongeVehicleData.this::getBaseVehicle);
        registerKeyValue(Keys.BASE_VEHICLE, ImmutableSpongeVehicleData.this::baseVehicle);
    }

}
