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

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePassengerData;
import org.spongepowered.api.data.manipulator.mutable.entity.PassengerData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongePassengerData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;
import org.spongepowered.common.util.GetterFunction;

public class ImmutableSpongePassengerData extends AbstractImmutableData<ImmutablePassengerData, PassengerData> implements ImmutablePassengerData {

    private final Entity vehicle;
    private final Entity baseVehicle;

    public ImmutableSpongePassengerData(Entity vehicle, Entity baseVehicle) {
        super(ImmutablePassengerData.class);
        this.vehicle = vehicle;
        this.baseVehicle = baseVehicle;
        registerStuff();
    }

    @Override
    public ImmutableBoundedValue<Entity> vehicle() {
        return ImmutableDataCachingUtil.getValue(ImmutableSpongeBoundedValue.class, Keys.VEHICLE, this.vehicle, this.vehicle);
    }

    @Override
    public ImmutableBoundedValue<Entity> baseVehicle() {
        return ImmutableDataCachingUtil.getValue(ImmutableSpongeBoundedValue.class, Keys.BASE_VEHICLE, this.baseVehicle, this.baseVehicle);
    }

    @Override
    public ImmutablePassengerData copy() {
        return new ImmutableSpongePassengerData(this.vehicle, this.baseVehicle);
    }

    @Override
    public PassengerData asMutable() {
        return new SpongePassengerData(this.vehicle, this.baseVehicle);
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(Keys.VEHICLE.getQuery(), this.vehicle).set(Keys.BASE_VEHICLE.getQuery(), this.baseVehicle);
    }

    @Override
    public int compareTo(ImmutablePassengerData o) {
        return ComparisonChain.start().compare(o.vehicle().get().getType().getName(), this.vehicle.getType().getName())
                .compare(o.baseVehicle().get().getType().getName(), this.baseVehicle.getType().getName())
                .compare(o.vehicle().get().getUniqueId(), this.vehicle.getUniqueId())
                .compare(o.baseVehicle().get().getUniqueId(), this.baseVehicle.getUniqueId()).result();
    }

    public Entity getVehicle() {
        return checkNotNull(this.vehicle);
    }

    public Entity getBaseVehicle() {
        return checkNotNull(this.baseVehicle);
    }

    private void registerStuff() {
        registerFieldGetter(Keys.VEHICLE, new GetterFunction<Object>() {

            @Override
            public Object get() {
                return getVehicle();
            }
        });

        registerKeyValue(Keys.VEHICLE, new GetterFunction<ImmutableValue<?>>() {

            @Override
            public ImmutableValue<?> get() {
                return vehicle();
            }
        });

        registerFieldGetter(Keys.BASE_VEHICLE, new GetterFunction<Object>() {

            @Override
            public Object get() {
                return getBaseVehicle();
            }
        });

        registerKeyValue(Keys.BASE_VEHICLE, new GetterFunction<ImmutableValue<?>>() {

            @Override
            public ImmutableValue<?> get() {
                return baseVehicle();
            }
        });
    }
}
