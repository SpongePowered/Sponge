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
package org.spongepowered.common.data.manipulators.entities;

import static org.spongepowered.api.data.DataQuery.of;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.entities.PassengerData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.data.manipulators.AbstractSingleValueData;

public class SpongePassengerData extends AbstractSingleValueData<Entity, PassengerData> implements PassengerData {

    public SpongePassengerData(Entity entity) {
        super(PassengerData.class, entity);
    }

    @Override
    public Entity getVehicle() {
        return this.getValue();
    }

    @Override
    public boolean setVehicle(Entity entity) {
        if (!entity.isLoaded()) {
            return false;
        } else {
            setValue(entity);
            return true;
        }
    }

    @Override
    public Entity getBaseVehicle() {
        net.minecraft.entity.Entity vehicle = ((net.minecraft.entity.Entity) this.value);
        while (vehicle.ridingEntity != null) {
            vehicle = vehicle.ridingEntity;
        }
        return ((Entity) vehicle);
    }

    @Override
    public PassengerData copy() {
        return new SpongePassengerData(this.getValue());
    }

    @Override
    public int compareTo(PassengerData o) {
        return o.getValue().getUniqueId().compareTo(this.getValue().getUniqueId());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(of("LeashHolder"), this.getValue().getUniqueId());
    }
}
